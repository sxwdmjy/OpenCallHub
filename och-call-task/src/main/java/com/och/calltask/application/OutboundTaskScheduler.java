package com.och.calltask.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundCustomerState;
import com.och.calltask.domain.OutboundTask;
import com.och.calltask.domain.OutboundTaskMode;
import com.och.calltask.domain.OutboundTaskState;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.calltask.mapper.OutboundTaskMapper;
import com.och.calltask.service.IPredictiveDialerService;
import com.och.calltask.service.IRobotOutboundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外呼任务定时调度器
 * <p>
 * 替代原 Quartz Job 体系的轻量级定时任务，基于 Spring {@code @Scheduled} 实现：
 * <ul>
 *   <li>预测拨号轮询（2s）—— 持续从 RUNNING 状态的 PREDICTIVE 任务队列中取号拨打</li>
 *   <li>机器人外呼轮询（5s）—— 持续从 RUNNING 状态的 ROBOT 任务队列中取号拨打</li>
 *   <li>重试客户恢复（30s）—— 将 RETRY_WAIT 且重试时间已到的客户转回 READY</li>
 *   <li>任务超时检查（60s）—— 超时未接听的 DIALING 客户标记为 ABANDONED</li>
 * </ul>
 * 由 {@code och.outbound.scheduler.enabled} 控制开关，默认开启。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "och.outbound.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class OutboundTaskScheduler {

    private final OutboundTaskMapper taskMapper;
    private final OutboundCustomerMapper customerMapper;
    private final IPredictiveDialerService predictiveDialerService;
    private final IRobotOutboundService robotOutboundService;

    /** 预测拨号间隔（毫秒） */
    private static final long PREDICTIVE_INTERVAL_MS = 2_000L;

    /** 重试客户恢复间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 30_000L;

    /** 任务超时检查间隔（毫秒） */
    private static final long TIMEOUT_INTERVAL_MS = 60_000L;

    /** 机器人外呼轮询间隔（毫秒） */
    private static final long ROBOT_INTERVAL_MS = 5_000L;

    // ==================== 预测拨号 ====================

    /**
     * 预测式外呼轮询
     * <p>
     * 每 2 秒扫描所有 RUNNING 状态且模式为 PREDICTIVE 的任务，
     * 对每一项调用 {@link PredictiveDialerService#dialNext(Long, Long)} 发起拨号。
     * 拨号速率由预测算法内部根据坐席空闲率和历史接通率动态计算。
     * </p>
     */
    @Scheduled(fixedDelay = PREDICTIVE_INTERVAL_MS)
    public void processPredictiveDialing() {
        List<OutboundTask> runningPredictiveTasks = findRunningPredictiveTasks();
        if (runningPredictiveTasks.isEmpty()) {
            return;
        }

        for (OutboundTask task : runningPredictiveTasks) {
            try {
                Long skillGroupId = resolveSkillGroupId(task);
                predictiveDialerService.dialNext(skillGroupId, task.getId());
            } catch (Exception e) {
                log.error("[OutboundTaskScheduler] 预测拨号异常: taskId={}, error={}",
                        task.getId(), e.getMessage(), e);
            }
        }
    }

    // ==================== 机器人外呼 ====================

    /**
     * 机器人外呼轮询
     * <p>
     * 每 5 秒扫描所有 RUNNING 状态且模式为 ROBOT 的任务，
     * 对每个任务查询 READY 状态客户（每次最多 10 个），
     * 逐个调用 {@link RobotOutboundService#startRobotCall} 发起机器人外呼。
     * 机器人配置（botType、flowId、versionId、systemPrompt）从任务的
     * {@code scheduleConfig} JSON 字段解析。
     * </p>
     */
    @Scheduled(fixedDelay = ROBOT_INTERVAL_MS)
    public void processRobotDialing() {
        List<OutboundTask> runningRobotTasks = findRunningRobotTasks();
        if (runningRobotTasks.isEmpty()) {
            return;
        }

        for (OutboundTask task : runningRobotTasks) {
            try {
                RobotConfig config = parseRobotConfig(task.getScheduleConfig());
                if (config == null) {
                    log.warn("[OutboundTaskScheduler] 机器人任务缺少配置: taskId={}", task.getId());
                    continue;
                }

                List<OutboundCustomer> readyCustomers = findReadyRobotCustomers(task.getId());
                for (OutboundCustomer customer : readyCustomers) {
                    try {
                        robotOutboundService.startRobotCall(
                                task.getId(), customer.getId(),
                                config.botType(), config.flowId(),
                                config.versionId(), config.systemPrompt());
                    } catch (Exception e) {
                        log.error("[OutboundTaskScheduler] 机器人外呼异常: taskId={}, customerId={}, error={}",
                                task.getId(), customer.getId(), e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("[OutboundTaskScheduler] 机器人任务处理异常: taskId={}, error={}",
                        task.getId(), e.getMessage(), e);
            }
        }
    }

    // ==================== 重试客户恢复 ====================

    /**
     * 重试客户恢复
     * <p>
     * 每 30 秒将 RETRY_WAIT 状态且 {@code nextRetryTime} 已到期（有值且 <= 当前时间）
     * 的客户转回 READY 状态，使其重新进入调度队列。
     * 对 nextRetryTime 为 null 的记录保持不动（说明重试策略未配置或已取消）。
     * </p>
     */
    @Scheduled(fixedDelay = RETRY_INTERVAL_MS)
    @Transactional
    public void processRetryCustomers() {
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<OutboundCustomer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OutboundCustomer::getState, OutboundCustomerState.RETRY_WAIT.name())
                .isNotNull(OutboundCustomer::getNextRetryTime)
                .le(OutboundCustomer::getNextRetryTime, now)
                .set(OutboundCustomer::getState, OutboundCustomerState.READY.name())
                .set(OutboundCustomer::getNextRetryTime, null)
                .set(OutboundCustomer::getUpdatedAt, now);

        customerMapper.update(wrapper);
    }

    // ==================== 任务超时检查 ====================

    /**
     * 任务超时检查
     * <p>
     * 每 60 秒检查所有 RUNNING/PAUSED 任务，将 DIALING 且超过
     * {@code abandonThreshold}（秒）的客户标记为 ABANDONED。
     * <p>
     * 超时计算：当前时间 - updatedAt（即进入 DIALING 的时间）> 任务配置的 abandonThreshold。
     * 未配置 abandonThreshold 的任务使用默认值 120 秒。
     * </p>
     */
    @Scheduled(fixedDelay = TIMEOUT_INTERVAL_MS)
    @Transactional
    public void processTaskTimeout() {
        List<OutboundTask> activeTasks = findActiveTasks();
        if (activeTasks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (OutboundTask task : activeTasks) {
            int threshold = task.getAbandonThreshold() != null && task.getAbandonThreshold() > 0
                    ? task.getAbandonThreshold() : 120;

            LocalDateTime deadline = now.minusSeconds(threshold);

            LambdaUpdateWrapper<OutboundCustomer> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(OutboundCustomer::getTaskId, task.getId())
                    .eq(OutboundCustomer::getState, OutboundCustomerState.DIALING.name())
                    .le(OutboundCustomer::getUpdatedAt, deadline)
                    .set(OutboundCustomer::getState, OutboundCustomerState.ABANDONED.name())
                    .set(OutboundCustomer::getUpdatedAt, now);

            customerMapper.update(wrapper);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 查询所有 RUNNING 状态且模式为 PREDICTIVE 的任务
     */
    private List<OutboundTask> findRunningPredictiveTasks() {
        LambdaQueryWrapper<OutboundTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundTask::getState, OutboundTaskState.RUNNING.name())
                .eq(OutboundTask::getMode, OutboundTaskMode.PREDICTIVE.name());
        return taskMapper.selectList(wrapper);
    }

    /**
     * 查询所有活跃任务（RUNNING 或 PAUSED）
     */
    private List<OutboundTask> findActiveTasks() {
        LambdaQueryWrapper<OutboundTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OutboundTask::getState,
                List.of(OutboundTaskState.RUNNING.name(), OutboundTaskState.PAUSED.name()));
        return taskMapper.selectList(wrapper);
    }

    /**
     * 从任务中解析技能组 ID
     * <p>使用中继（trunkId）作为技能组标识，中继是坐席分组的自然桥梁。
     * 如果 trunkId 为 null，回退使用 taskId 本身，保证 dialNext 正常运作。</p>
     */
    private Long resolveSkillGroupId(OutboundTask task) {
        if (task.getTrunkId() != null && task.getTrunkId() > 0) {
            return task.getTrunkId();
        }
        return task.getId();
    }

    /**
     * 查询所有 RUNNING 状态且模式为 ROBOT 的任务
     */
    private List<OutboundTask> findRunningRobotTasks() {
        LambdaQueryWrapper<OutboundTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundTask::getState, OutboundTaskState.RUNNING.name())
                .eq(OutboundTask::getMode, OutboundTaskMode.ROBOT.name());
        return taskMapper.selectList(wrapper);
    }

    /**
     * 查询任务下 READY 状态的客户（每次最多 10 个）
     */
    private List<OutboundCustomer> findReadyRobotCustomers(Long taskId) {
        LambdaQueryWrapper<OutboundCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundCustomer::getTaskId, taskId)
                .eq(OutboundCustomer::getState, OutboundCustomerState.READY.name())
                .orderByAsc(OutboundCustomer::getId)
                .last("LIMIT 10");
        return customerMapper.selectList(wrapper);
    }

    /**
     * 从任务的 scheduleConfig JSON 字段解析机器人配置
     *
     * @param scheduleConfig JSON 字符串，预期格式：
     *                       {"botType":"INTERNAL","flowId":1,"versionId":1,"systemPrompt":"..."}
     * @return 解析后的 RobotConfig，解析失败或字段缺失返回 null
     */
    private RobotConfig parseRobotConfig(String scheduleConfig) {
        if (scheduleConfig == null || scheduleConfig.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(scheduleConfig);
            String botType = node.has("botType") ? node.get("botType").asText() : "INTERNAL";
            Long flowId = node.has("flowId") ? node.get("flowId").asLong() : null;
            Long versionId = node.has("versionId") ? node.get("versionId").asLong() : null;
            String systemPrompt = node.has("systemPrompt") ? node.get("systemPrompt").asText() : null;
            if (flowId == null || versionId == null) {
                log.warn("[OutboundTaskScheduler] 机器人配置缺少 flowId 或 versionId: {}", scheduleConfig);
                return null;
            }
            return new RobotConfig(botType, flowId, versionId, systemPrompt);
        } catch (Exception e) {
            log.error("[OutboundTaskScheduler] 解析机器人配置失败: scheduleConfig={}, error={}",
                    scheduleConfig, e.getMessage());
            return null;
        }
    }

    /**
     * 机器人任务配置记录
     *
     * @param botType      机器人引擎类型，默认 INTERNAL
     * @param flowId       机器人流程 ID
     * @param versionId    流程版本 ID
     * @param systemPrompt 系统提示词
     */
    private record RobotConfig(String botType, Long flowId, Long versionId, String systemPrompt) {}
}
