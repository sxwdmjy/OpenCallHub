package com.och.calltask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.ai.service.impl.RobotFlowEngineImpl;
import com.och.ai.domain.RobotSession;
import com.och.ai.mapper.RobotSessionMapper;
import com.och.calltask.application.OutboundCustomerStateMachine;
import com.och.calltask.domain.*;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 机器人外呼引擎
 * <p>
 * 实现 {@link OutboundTaskMode#ROBOT} 模式的完整外呼流程：
 * 发起机器人会话、处理用户实时回复、完成回调更新外呼状态。
 * </p>
 *
 * <h3>核心流程</h3>
 * <ol>
 *   <li>{@link #startRobotCall} — 创建拨号尝试 + 启动机器人会话，客户状态 READY/SCHEDULED -> DIALING</li>
 *   <li>{@link #handleRobotResponse} — 委托 {@link RobotFlowEngineImpl#processTurn} 进行实时对话</li>
 *   <li>{@link #onRobotComplete} — 结束机器人会话，客户状态 CONNECTED -> FINISHED，更新拨号尝试</li>
 * </ol>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>委派模式</b>：机器人对话处理委托给 och-ai 模块的 {@link RobotFlowEngineImpl}，本服务专注于外呼业务编排</li>
 *   <li><b>状态机模式</b>：通过 {@link OutboundCustomerStateMachine} 确保客户状态流转合法性</li>
 * </ul>
 *
 * @see OutboundTaskMode#ROBOT
 * @see RobotFlowEngineImpl
 * @see OutboundCustomerStateMachine
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotOutboundServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.OutboundCustomerMapper, com.och.calltask.domain.OutboundCustomer> implements com.och.calltask.service.IRobotOutboundService {

    private final OutboundDialAttemptMapper attemptMapper;
    private final OutboundCustomerStateMachine stateMachine;
    private final RobotFlowEngineImpl robotFlowEngine;
    private final RobotSessionMapper robotSessionMapper;

    /**
     * 发起机器人外呼。
     * <p>为指定客户创建拨号尝试记录，启动 AI 机器人会话，并将 customer 状态从
     * READY/SCHEDULED 流转至 DIALING。</p>
     *
     * @param taskId       外呼任务 ID
     * @param customerId   客户 ID
     * @param botType      机器人引擎类型（DIFY / RAGFLOW / LANGFLOW / INTERNAL）
     * @param flowId       机器人流程 ID
     * @param versionId    流程版本 ID
     * @param systemPrompt 系统提示词
     * @return 机器人会话 ID（用于后续对话和完成回调）
     * @throws BizException 客户不存在或状态流转不合法时抛出
     */
    @Transactional
    public String startRobotCall(Long taskId, Long customerId, String botType,
                                  Long flowId, Long versionId, String systemPrompt) {
        // 1. 校验客户归属
        OutboundCustomer customer = baseMapper.selectById(customerId);
        if (customer == null || !Objects.equals(customer.getTaskId(), taskId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "客户不存在");
        }

        // 2. 状态流转: READY / SCHEDULED -> DIALING
        stateMachine.validate(OutboundCustomerState.valueOf(customer.getState()),
                OutboundCustomerState.DIALING);

        int attemptNo = (customer.getAttemptCount() != null ? customer.getAttemptCount() : 0) + 1;
        customer.setState(OutboundCustomerState.DIALING.name());
        customer.setAttemptCount(attemptNo);
        baseMapper.updateById(customer);

        // 3. 创建拨号尝试
        String dedupKey = taskId + ":" + customerId + ":" + attemptNo;
        String callId = "robot_" + dedupKey;
        OutboundDialAttempt attempt = OutboundDialAttempt.builder()
                .taskId(taskId)
                .customerId(customerId)
                .attemptNo(attemptNo)
                .dedupKey(dedupKey)
                .callId(callId)
                .state("DIALING")
                .dialTime(LocalDateTime.now())
                .build();
        attemptMapper.insert(attempt);

        // 4. 启动 AI 机器人会话
        RobotSession session = robotFlowEngine.startSession(callId, flowId, versionId);
        String sessionId = session.getSessionId();

        // 5. 将机器人会话 ID 写回客户记录，便于后续回调定位
        customer.setRobotSessionId(sessionId);
        baseMapper.updateById(customer);

        // 6. 机器人引擎首次问候（异步或由外部呼叫平台在电话接通后触发）
        log.info("[RobotOutbound] 发起机器人外呼: taskId={}, customerId={}, sessionId={}, callId={}, botType={}",
                taskId, customerId, sessionId, callId, botType);

        return sessionId;
    }

    /**
     * 处理机器人对话中的用户语音回复。
     * <p>将用户语音转文字后的文本提交给 AI 引擎，返回机器人的文本回复、意图和是否转人工。</p>
     *
     * @param sessionId    机器人会话 ID（由 {@link #startRobotCall} 返回）
     * @param userInput    用户语音识别后的文本
     * @param botType      机器人引擎类型
     * @param systemPrompt 系统提示词
     * @return 本轮对话结果：回复文本、意图、是否需要转人工、延迟毫秒
     */
    @Transactional
    public RobotFlowEngineImpl.TurnResult handleRobotResponse(String sessionId, String userInput,
                                                           String botType, String systemPrompt) {
        RobotFlowEngineImpl.TurnResult result = robotFlowEngine.processTurn(sessionId, userInput, botType, systemPrompt);

        log.info("[RobotOutbound] 处理用户回复: sessionId={}, intent={}, needTransfer={}, latencyMs={}",
                sessionId, result.intent(), result.needTransfer(), result.latencyMs());

        return result;
    }

    /**
     * 机器人对话完成回调。
     * <p>根据拨号结果执行对应的状态流转和记录更新：</p>
     * <ul>
     *   <li>{@link DialResult#CONNECTED} — 客户 DIALING -> CONNECTED，更新拨号尝试为已完成</li>
     *   <li>{@link DialResult#ROBOT_HANDOFF} — 客户转回 READY 待人工处理，更新拨号尝试为已完成</li>
     *   <li>其他结果 — 保持原有逻辑，若客户当前为 CONNECTED 则流转至 FINISHED</li>
     * </ul>
     *
     * @param sessionId 机器人会话 ID
     * @param result    拨号结果
     */
    @Transactional
    public void onRobotComplete(String sessionId, DialResult result) {
        // 1. 通过 robotSessionId 查找客户
        OutboundCustomer customer = baseMapper.selectOne(
                new LambdaQueryWrapper<OutboundCustomer>()
                        .eq(OutboundCustomer::getRobotSessionId, sessionId));

        // 2. 结束 AI 会话
        robotFlowEngine.endSession(sessionId, result.name());

        if (customer != null) {
            OutboundCustomerState currentState = OutboundCustomerState.valueOf(customer.getState());

            if (result == DialResult.ROBOT_HANDOFF) {
                // 机器人转人工：客户状态回退到 READY
                stateMachine.validate(currentState, OutboundCustomerState.READY);
                customer.setState(OutboundCustomerState.READY.name());
                baseMapper.updateById(customer);
                log.info("[RobotOutbound] 机器人转人工: sessionId={}, customerId={}", sessionId, customer.getId());

            } else if (result == DialResult.CONNECTED) {
                // 已接通且对话完成：DIALING -> CONNECTED（后续可由其他流程流转至 FINISHED）
                if (currentState == OutboundCustomerState.DIALING) {
                    stateMachine.validate(currentState, OutboundCustomerState.CONNECTED);
                    customer.setState(OutboundCustomerState.CONNECTED.name());
                    baseMapper.updateById(customer);
                    log.info("[RobotOutbound] 机器人呼叫已接通完成: sessionId={}, customerId={}",
                            sessionId, customer.getId());
                } else {
                    log.warn("[RobotOutbound] 对话完成但客户状态为 {}，无法流转至 CONNECTED: sessionId={}, customerId={}",
                            currentState, sessionId, customer.getId());
                }

            } else {
                // 其他结果保持原有逻辑：仅 CONNECTED 状态允许流转到 FINISHED
                if (currentState == OutboundCustomerState.CONNECTED) {
                    stateMachine.validate(currentState, OutboundCustomerState.FINISHED);
                    customer.setState(OutboundCustomerState.FINISHED.name());
                    baseMapper.updateById(customer);
                } else {
                    log.warn("[RobotOutbound] 对话完成但客户状态为 {}，不流转至 FINISHED: sessionId={}, customerId={}",
                            currentState, sessionId, customer.getId());
                }
            }

            // 更新拨号尝试
            updateDialAttempt(sessionId, result);
        }

        log.info("[RobotOutbound] 机器人对话完成: sessionId={}, result={}, customerId={}",
                sessionId, result, customer != null ? customer.getId() : "unknown");
    }

    /**
     * 根据机器人会话 ID 找到对应的拨号尝试，更新其结束时间、状态和拨号结果。
     */
    private void updateDialAttempt(String sessionId, DialResult result) {
        RobotSession session = robotSessionMapper.selectOne(
                new LambdaQueryWrapper<RobotSession>()
                        .eq(RobotSession::getSessionId, sessionId)
                        .last("LIMIT 1"));
        if (session == null) {
            log.warn("[RobotOutbound] 未找到 RobotSession: sessionId={}", sessionId);
            return;
        }

        OutboundDialAttempt attempt = attemptMapper.selectOne(
                new LambdaQueryWrapper<OutboundDialAttempt>()
                        .eq(OutboundDialAttempt::getCallId, session.getCallId())
                        .last("LIMIT 1"));
        if (attempt != null) {
            attempt.setState("COMPLETED");
            attempt.setDialResult(result.name());
            attempt.setEndTime(LocalDateTime.now());
            if (attempt.getDialTime() != null) {
                Duration duration = Duration.between(attempt.getDialTime(), attempt.getEndTime());
                attempt.setDurationSeconds((int) duration.getSeconds());
            }
            attemptMapper.updateById(attempt);
            log.info("[RobotOutbound] 拨号尝试 {} 已完成: result={}, duration={}s",
                    attempt.getId(), result, attempt.getDurationSeconds());
        }
    }
}
