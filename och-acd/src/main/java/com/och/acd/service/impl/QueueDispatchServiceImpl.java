package com.och.acd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.acd.application.strategy.DispatchStrategy;
import com.och.acd.application.strategy.DispatchStrategyFactory;
import com.och.acd.domain.AcdAgentStatus;
import com.och.acd.domain.AcdQueueSession;
import com.och.acd.domain.AgentState;
import com.och.acd.domain.SkillGroup;
import com.och.acd.mapper.AcdAgentStatusMapper;
import com.och.acd.mapper.AcdQueueSessionMapper;
import com.och.acd.mapper.SkillGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ACD 队列分配服务
 * <p>
 * 呼叫入队、策略分配、座席释放、状态管理的核心编排服务。
 * 使用 MyBatis-Plus LambdaQueryWrapper 构建类型安全的查询条件。
 * </p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>策略模式</b>：DispatchStrategy 定义坐席选择算法，支持 IDLE_FIRST、ROUND_ROBIN、LEAST_CALLS、SKILL_PRIORITY</li>
 *   <li><b>工厂模式</b>：DispatchStrategyFactory 按技能组配置的策略名称获取策略实例</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueDispatchServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.acd.mapper.AcdQueueSessionMapper, com.och.acd.domain.AcdQueueSession> {

    private final AcdAgentStatusMapper agentStatusMapper;
    private final SkillGroupMapper skillGroupMapper;
    private final DispatchStrategyFactory strategyFactory;

    // ==================== 入队 ====================

    @Transactional
    public AcdQueueSession enqueue(Long skillGroupId, String callId, Long callSessionId, String callerNumber) {
        int position = baseMapper.selectCount(
                new LambdaQueryWrapper<AcdQueueSession>()
                        .eq(AcdQueueSession::getSkillGroupId, skillGroupId)
                        .eq(AcdQueueSession::getState, "WAITING")).intValue() + 1;

        AcdQueueSession session = AcdQueueSession.builder()
                .skillGroupId(skillGroupId).callId(callId).callSessionId(callSessionId)
                .callerNumber(callerNumber).state("WAITING").position(position)
                .enterTime(LocalDateTime.now()).build();
        baseMapper.insert(session);

        log.info("[ACD] 呼叫入队: callId={}, skillGroup={}, position={}", callId, skillGroupId, position);
        tryDispatch(skillGroupId);
        return session;
    }

    // ==================== 分配 ====================

    @Transactional
    public AcdAgentStatus tryDispatch(Long skillGroupId) {
        // 查询等待队列（按进入时间升序，Lambda 排序）
        LambdaQueryWrapper<AcdQueueSession> queueWp = new LambdaQueryWrapper<AcdQueueSession>()
                .eq(AcdQueueSession::getSkillGroupId, skillGroupId)
                .eq(AcdQueueSession::getState, "WAITING")
                .orderByAsc(AcdQueueSession::getEnterTime);
        List<AcdQueueSession> waitingQueue = baseMapper.selectList(queueWp);

        if (waitingQueue.isEmpty()) return null;

        // 读取技能组配置的分配策略
        SkillGroup skillGroup = skillGroupMapper.selectById(skillGroupId);
        String strategyName = skillGroup != null && skillGroup.getDispatchStrategy() != null
                ? skillGroup.getDispatchStrategy() : "IDLE_FIRST";
        DispatchStrategy strategy = strategyFactory.getStrategy(strategyName);

        // 查询空闲坐席（按策略排序）
        List<AcdAgentStatus> idleAgents = getSortedIdleAgents(skillGroupId, strategyName);
        if (idleAgents.isEmpty()) {
            log.debug("[ACD] 无空闲坐席: skillGroup={}, waiting={}", skillGroupId, waitingQueue.size());
            return null;
        }

        AcdAgentStatus selected = strategy.select(idleAgents);
        if (selected == null) {
            log.debug("[ACD] 策略未选出坐席: skillGroup={}, strategy={}", skillGroupId, strategyName);
            return null;
        }

        return dispatch(selected, waitingQueue.get(0));
    }

    private List<AcdAgentStatus> getSortedIdleAgents(Long skillGroupId, String strategyName) {
        LambdaQueryWrapper<AcdAgentStatus> wrapper = new LambdaQueryWrapper<AcdAgentStatus>()
                .eq(AcdAgentStatus::getSkillGroupId, skillGroupId)
                .eq(AcdAgentStatus::getState, AgentState.IDLE.name());

        // 按策略类型决定排序方式
        if ("SKILL_PRIORITY".equals(strategyName)) {
            wrapper.orderByDesc(AcdAgentStatus::getSkillLevel)
                   .orderByAsc(AcdAgentStatus::getLastStateTime);
        } else {
            wrapper.orderByAsc(AcdAgentStatus::getLastStateTime);
        }
        return agentStatusMapper.selectList(wrapper);
    }

    @Transactional
    public AcdAgentStatus dispatch(AcdAgentStatus agent, AcdQueueSession call) {
        int currentCalls = agent.getCurrentCalls() != null ? agent.getCurrentCalls() : 0;
        int maxConcurrency = agent.getMaxConcurrency() != null ? agent.getMaxConcurrency() : 1;

        if (currentCalls >= maxConcurrency) {
            log.warn("[ACD] 坐席达到最大并发: agentId={}, calls={}/{}", agent.getAgentId(), currentCalls, maxConcurrency);
            return null;
        }

        agent.setState(AgentState.BUSY.name());
        agent.setCurrentCalls(currentCalls + 1);
        agent.setTotalAnswered(agent.getTotalAnswered() != null ? agent.getTotalAnswered() + 1 : 1);
        agent.setLastCallTime(LocalDateTime.now());
        agent.setLastStateTime(LocalDateTime.now());
        agentStatusMapper.updateById(agent);

        call.setState("ASSIGNED");
        call.setAssignedAgentId(agent.getId());
        call.setAssignedTime(LocalDateTime.now());
        call.setWaitSeconds((int) Duration.between(call.getEnterTime(), LocalDateTime.now()).getSeconds());
        baseMapper.updateById(call);

        log.info("[ACD] 分配成功: callId={} -> agentId={}, skillGroup={}, waitSeconds={}",
                call.getCallId(), agent.getAgentId(), call.getSkillGroupId(), call.getWaitSeconds());
        return agent;
    }

    // ==================== 状态管理 ====================

    @Transactional
    public void updateAgentState(Long agentId, Long skillGroupId, AgentState newState) {
        LambdaQueryWrapper<AcdAgentStatus> wrapper = new LambdaQueryWrapper<AcdAgentStatus>()
                .eq(AcdAgentStatus::getAgentId, agentId)
                .eq(AcdAgentStatus::getSkillGroupId, skillGroupId);
        AcdAgentStatus status = agentStatusMapper.selectOne(wrapper);

        if (status == null) {
            status = AcdAgentStatus.builder()
                    .agentId(agentId).skillGroupId(skillGroupId).state(newState.name())
                    .skillLevel(1).maxConcurrency(1).currentCalls(0).totalAnswered(0)
                    .lastStateTime(LocalDateTime.now()).build();
            agentStatusMapper.insert(status);
            log.info("[ACD] 首次签入: agentId={}, skillGroup={}, state={}", agentId, skillGroupId, newState.getLabel());
            return;
        }

        status.setState(newState.name());
        status.setLastStateTime(LocalDateTime.now());
        agentStatusMapper.updateById(status);
        log.info("[ACD] 状态变更: agentId={}, state={}", agentId, newState.getLabel());

        if (newState.isAvailable()) {
            tryDispatch(skillGroupId);
        }
    }

    // ==================== 释放 ====================

    @Transactional
    public void releaseAgent(Long callSessionId, String endCause) {
        LambdaQueryWrapper<AcdQueueSession> wrapper = new LambdaQueryWrapper<AcdQueueSession>()
                .eq(AcdQueueSession::getCallSessionId, callSessionId)
                .eq(AcdQueueSession::getState, "ASSIGNED");
        AcdQueueSession session = baseMapper.selectOne(wrapper);

        if (session == null) return;

        session.setState("ENDED");
        session.setEndTime(LocalDateTime.now());
        session.setEndCause(endCause);
        baseMapper.updateById(session);

        if (session.getAssignedAgentId() != null) {
            AcdAgentStatus agent = agentStatusMapper.selectById(session.getAssignedAgentId());
            if (agent != null) {
                agent.setState(AgentState.AFTER_CALL.name());
                agent.setCurrentCalls(Math.max(0, (agent.getCurrentCalls() != null ? agent.getCurrentCalls() : 1) - 1));
                agent.setLastStateTime(LocalDateTime.now());
                agentStatusMapper.updateById(agent);

                log.info("[ACD] 释放坐席: agentId={}, endCause={}, remainingCalls={}",
                        agent.getAgentId(), endCause, agent.getCurrentCalls());

                if (agent.getCurrentCalls() == 0) {
                    tryDispatch(session.getSkillGroupId());
                }
            }
        }
    }

    // ==================== 统计 ====================

    public QueueStats getQueueStats(Long skillGroupId) {
        int waiting = baseMapper.selectCount(
                new LambdaQueryWrapper<AcdQueueSession>()
                        .eq(AcdQueueSession::getSkillGroupId, skillGroupId)
                        .eq(AcdQueueSession::getState, "WAITING")).intValue();

        long totalAgents = agentStatusMapper.selectCount(
                new LambdaQueryWrapper<AcdAgentStatus>()
                        .eq(AcdAgentStatus::getSkillGroupId, skillGroupId));

        long idleAgents = agentStatusMapper.selectCount(
                new LambdaQueryWrapper<AcdAgentStatus>()
                        .eq(AcdAgentStatus::getSkillGroupId, skillGroupId)
                        .eq(AcdAgentStatus::getState, AgentState.IDLE.name()));

        // SLA 统计：平均等待时间和最大等待时间
        List<AcdQueueSession> waitingList = baseMapper.selectList(
                new LambdaQueryWrapper<AcdQueueSession>()
                        .eq(AcdQueueSession::getSkillGroupId, skillGroupId)
                        .eq(AcdQueueSession::getState, "WAITING"));
        LocalDateTime now = LocalDateTime.now();
        int avgWaitSeconds = waitingList.isEmpty() ? 0
                : (int) waitingList.stream().mapToLong(w -> Duration.between(w.getEnterTime(), now).getSeconds()).average().orElse(0);
        int maxWaitSeconds = waitingList.isEmpty() ? 0
                : (int) waitingList.stream().mapToLong(w -> Duration.between(w.getEnterTime(), now).getSeconds()).max().orElse(0);

        return new QueueStats(waiting, totalAgents, idleAgents, avgWaitSeconds, maxWaitSeconds);
    }

    /** 队列统计快照（含 SLA 指标） */
    public record QueueStats(int waitingCalls, long totalAgents, long idleAgents,
                             int avgWaitSeconds, int maxWaitSeconds) {
        public int availableAgents() { return (int) idleAgents; }
        /** SLA 达标率（平均等待小于30秒即为达标） */
        public double slaRate(int thresholdSeconds) {
            return avgWaitSeconds <= thresholdSeconds ? 1.0 : 0.0;
        }
    }
}