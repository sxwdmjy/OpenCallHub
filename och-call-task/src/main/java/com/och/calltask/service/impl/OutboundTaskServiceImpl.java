package com.och.calltask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.calltask.application.OutboundTaskStateMachine;
import com.och.calltask.domain.*;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import com.och.calltask.mapper.OutboundTaskMapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.security.audit.Audited;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 外呼任务服务
 * <p>
 * 管理外呼任务的完整生命周期，包括创建、提交、启动、暂停、取消等操作，
 * 所有状态变更均通过 {@link OutboundTaskStateMachine} 校验合法性。
 * 同时提供任务配置更新、进度查询和统计指标查询功能。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundTaskServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.OutboundTaskMapper, com.och.calltask.domain.OutboundTask> implements com.och.calltask.service.IOutboundTaskService {

    private final OutboundTaskStateMachine taskStateMachine;
    private final OutboundCustomerMapper customerMapper;
    private final OutboundDialAttemptMapper dialAttemptMapper;

    /**
     * 创建外呼任务，初始状态为 DRAFT（草稿）
     */
    @Transactional
    @Audited(action = "CREATE_OUTBOUND_TASK", targetType = "OUTBOUND_TASK")
    public OutboundTask createTask(OutboundTask task) {
        task.setState(OutboundTaskState.DRAFT.name());
        baseMapper.insert(task);
        log.info("[OutboundTask] Created: id={}, name={}, mode={}", task.getId(), task.getTaskName(), task.getMode());
        return task;
    }

    /**
     * 提交任务，状态从 DRAFT 变为 READY（就绪）
     */
    @Transactional
    public OutboundTask submitTask(Long taskId) {
        OutboundTask task = baseMapper.selectById(taskId);
        if (task == null) throw new BizException(ErrorCode.NOT_FOUND, "任务不存在");
        taskStateMachine.validate(OutboundTaskState.valueOf(task.getState()), OutboundTaskState.READY);
        task.setState(OutboundTaskState.READY.name());
        baseMapper.updateById(task);
        return task;
    }

    /**
     * 启动任务，状态从 READY 变为 RUNNING（运行中）
     */
    @Transactional
    public OutboundTask startTask(Long taskId) {
        OutboundTask task = baseMapper.selectById(taskId);
        if (task == null) throw new BizException(ErrorCode.NOT_FOUND, "任务不存在");
        taskStateMachine.validate(OutboundTaskState.valueOf(task.getState()), OutboundTaskState.RUNNING);
        task.setState(OutboundTaskState.RUNNING.name());
        baseMapper.updateById(task);
        return task;
    }

    /**
     * 暂停任务，状态从 RUNNING 变为 PAUSED（暂停）
     */
    @Transactional
    public OutboundTask pauseTask(Long taskId) {
        OutboundTask task = baseMapper.selectById(taskId);
        if (task == null) throw new BizException(ErrorCode.NOT_FOUND, "任务不存在");
        taskStateMachine.validate(OutboundTaskState.valueOf(task.getState()), OutboundTaskState.PAUSED);
        task.setState(OutboundTaskState.PAUSED.name());
        baseMapper.updateById(task);
        return task;
    }

    /**
     * 取消任务，状态变为 CANCELLED（已取消）
     */
    @Transactional
    public OutboundTask cancelTask(Long taskId) {
        OutboundTask task = baseMapper.selectById(taskId);
        if (task == null) throw new BizException(ErrorCode.NOT_FOUND, "任务不存在");
        taskStateMachine.validate(OutboundTaskState.valueOf(task.getState()), OutboundTaskState.CANCELLED);
        task.setState(OutboundTaskState.CANCELLED.name());
        baseMapper.updateById(task);
        return task;
    }

    /**
     * 分页查询任务列表
     */
    public IPage<OutboundTask> listTasks(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<OutboundTask> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(OutboundTask::getTaskName, keyword);
        }
        wrapper.orderByDesc(OutboundTask::getCreatedAt);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 按ID查询任务详情
     */
    public OutboundTask getTask(Long id) {
        return baseMapper.selectById(id);
    }

    // ==================== 任务配置与统计方法 ====================

    /**
     * 更新任务配置
     * <p>
     * 允许在任务未启动时（DRAFT 或 READY 状态）修改轮次配置、重试策略、过滤配置等。
     * 运行中的任务不允许修改核心配置，需要先暂停。
     * </p>
     *
     * @param taskId     任务 ID
     * @param roundsConf 配置参数 Map，支持的 key：
     *                   <ul>
     *                     <li>scheduleConfig - 调度配置（JSON字符串）</li>
     *                     <li>retryConfig - 重试策略配置（JSON字符串）</li>
     *                     <li>filterConfig - 过滤配置（JSON字符串）</li>
     *                     <li>maxConcurrency - 最大并发数</li>
     *                     <li>abandonThreshold - 放弃阈值（秒）</li>
     *                   </ul>
     * @return 更新后的任务对象
     */
    @Transactional
    public OutboundTask updateTaskConfig(Long taskId, Map<String, Object> roundsConf) {
        OutboundTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        // 运行中的任务不允许修改核心配置
        OutboundTaskState currentState = OutboundTaskState.valueOf(task.getState());
        if (currentState.isActive()) {
            throw new BizException(ErrorCode.BIZ_OPERATION_DENIED,
                    "运行中的任务不允许修改配置，请先暂停任务");
        }

        // 按 key 更新对应字段
        if (roundsConf.containsKey("scheduleConfig")) {
            task.setScheduleConfig(String.valueOf(roundsConf.get("scheduleConfig")));
        }
        if (roundsConf.containsKey("retryConfig")) {
            task.setRetryConfig(String.valueOf(roundsConf.get("retryConfig")));
        }
        if (roundsConf.containsKey("filterConfig")) {
            task.setFilterConfig(String.valueOf(roundsConf.get("filterConfig")));
        }
        if (roundsConf.containsKey("maxConcurrency")) {
            task.setMaxConcurrency(Integer.valueOf(roundsConf.get("maxConcurrency").toString()));
        }
        if (roundsConf.containsKey("abandonThreshold")) {
            task.setAbandonThreshold(Integer.valueOf(roundsConf.get("abandonThreshold").toString()));
        }

        baseMapper.updateById(task);
        log.info("[OutboundTask] Config updated: id={}", taskId);
        return task;
    }

    /**
     * 查询任务进度
     * <p>
     * 返回任务的执行进度信息，包括客户总数、已完成数、待处理数、接通数和成功率。
     * 指标来自 OutboundTask 实体的预计算字段和 OutboundCustomer 的实时统计。
     * </p>
     *
     * @param taskId 任务 ID
     * @return 任务进度信息 Map，包含以下 key：
     *         <ul>
     *           <li>totalCustomers - 客户总数</li>
     *           <li>completedCustomers - 已完成客户数</li>
     *           <li>pendingCustomers - 待处理客户数</li>
     *           <li>connectedCount - 接通数</li>
     *           <li>successRate - 成功率（接通数 / 已完成数，百分比字符串）</li>
     *           <li>progressPercent - 进度百分比（已完成数 / 总数 × 100）</li>
     *           <li>state - 任务当前状态</li>
     *         </ul>
     */
    public Map<String, Object> getTaskProgress(Long taskId) {
        OutboundTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        int total = task.getTotalCustomers() != null ? task.getTotalCustomers() : 0;
        int completed = task.getCompletedCustomers() != null ? task.getCompletedCustomers() : 0;
        int connected = task.getConnectedCount() != null ? task.getConnectedCount() : 0;

        // 实时统计待处理客户数
        int pending = customerMapper.countPending(taskId);

        // 计算进度百分比（保留两位小数）
        BigDecimal progressPercent = total > 0
                ? BigDecimal.valueOf(completed).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算接通成功率（接通数 / 已完成数）
        String successRate = completed > 0
                ? BigDecimal.valueOf(connected).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP)
                        .toString() + "%"
                : "0%";

        return Map.of(
                "totalCustomers", total,
                "completedCustomers", completed,
                "pendingCustomers", pending,
                "connectedCount", connected,
                "successRate", successRate,
                "progressPercent", progressPercent,
                "state", task.getState()
        );
    }

    /**
     * 查询任务统计指标
     * <p>
     * 基于 OutboundDialAttempt 拨号记录计算任务的接通率、平均通话时长等核心指标。
     * 数据来源为拨号尝试表，反映实际拨号维度的统计情况。
     * </p>
     *
     * @param taskId 任务 ID
     * @return 统计指标 Map，包含以下 key：
     *         <ul>
     *           <li>totalAttempts - 总拨号尝试次数</li>
     *           <li>connectedAttempts - 接通次数</li>
     *           <li>answerRate - 接通率（百分比字符串，如 "45.50%"）</li>
     *           <li>avgTalkSeconds - 平均通话时长（秒）</li>
     *           <li>totalTalkSeconds - 累计通话时长（秒）</li>
     *           <li>noAnswerCount - 无应答次数</li>
     *           <li>busyCount - 占线次数</li>
     *           <li>failedCount - 失败次数</li>
     *           <li>conversionRate - 转化率（接通且通话时长 > 0 的占比）</li>
     *         </ul>
     */
    public Map<String, Object> getTaskMetrics(Long taskId) {
        // 确保任务存在
        OutboundTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        // 查询该任务下所有拨号尝试记录
        LambdaQueryWrapper<OutboundDialAttempt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundDialAttempt::getTaskId, taskId);
        List<OutboundDialAttempt> allAttempts = dialAttemptMapper.selectList(wrapper);

        int totalAttempts = allAttempts.size();
        int connectedAttempts = 0;
        int noAnswerCount = 0;
        int busyCount = 0;
        int failedCount = 0;
        long totalTalkSeconds = 0;
        int effectiveTalkCount = 0; // 通话时长 > 0 的接通记录数

        // 遍历所有拨号记录，统计各类指标
        for (OutboundDialAttempt attempt : allAttempts) {
            String dialResult = attempt.getDialResult();
            if (dialResult == null) continue;

            switch (dialResult) {
                case "CONNECTED":
                    connectedAttempts++;
                    // 累计通话时长
                    if (attempt.getDurationSeconds() != null && attempt.getDurationSeconds() > 0) {
                        totalTalkSeconds += attempt.getDurationSeconds();
                        effectiveTalkCount++;
                    }
                    break;
                case "NO_ANSWER":
                    noAnswerCount++;
                    break;
                case "BUSY":
                    busyCount++;
                    break;
                case "FAILED":
                    failedCount++;
                    break;
                default:
                    break;
            }
        }

        // 计算接通率（百分比）
        String answerRate = totalAttempts > 0
                ? BigDecimal.valueOf(connectedAttempts).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalAttempts), 2, RoundingMode.HALF_UP)
                        .toString() + "%"
                : "0%";

        // 计算平均通话时长（秒）
        BigDecimal avgTalkSeconds = effectiveTalkCount > 0
                ? BigDecimal.valueOf(totalTalkSeconds)
                        .divide(BigDecimal.valueOf(effectiveTalkCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算转化率（有效通话数 / 总尝试数）
        String conversionRate = totalAttempts > 0
                ? BigDecimal.valueOf(effectiveTalkCount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalAttempts), 2, RoundingMode.HALF_UP)
                        .toString() + "%"
                : "0%";

        return Map.of(
                "totalAttempts", totalAttempts,
                "connectedAttempts", connectedAttempts,
                "answerRate", answerRate,
                "avgTalkSeconds", avgTalkSeconds,
                "totalTalkSeconds", totalTalkSeconds,
                "noAnswerCount", noAnswerCount,
                "busyCount", busyCount,
                "failedCount", failedCount,
                "conversionRate", conversionRate
        );
    }
}
