package com.och.call.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.call.domain.CallRecord;
import com.och.call.application.CallStateMachine;
import com.och.call.domain.CallSession;
import com.och.call.application.CallStateMachine;
import com.och.call.domain.CallState;
import com.och.call.application.CallStateMachine;
import com.och.call.domain.ServiceRecord;
import com.och.call.application.CallStateMachine;
import com.och.call.mapper.CallRecordMapper;
import com.och.call.mapper.CallSessionMapper;
import com.och.call.mapper.ServiceRecordMapper;
import com.och.esl.application.PhoneControlCommandService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电话条应用服务。
 * <p>
 * 负责座席电话条动作的业务编排：查询通话会话、校验通话状态、发送 ESL 控制命令、
 * 更新本地通话状态。Controller 只保留 HTTP 参数和响应封装，避免把通信细节暴露到接口层。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneBarApplicationServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.call.mapper.CallSessionMapper, com.och.call.domain.CallSession> implements com.och.call.service.IPhoneBarService {

    private final CallRecordMapper callRecordMapper;
    private final ServiceRecordMapper serviceRecordMapper;
    private final AfterCallWorkServiceImpl afterCallWorkService;
    private final CallStateMachine stateMachine;
    private final PhoneControlCommandService phoneControlService;

    @Transactional
    public void answer(String callId) {
        CallSession session = findSession(callId, "接听");
        if (session == null) {
            return;
        }
        if (!sendCommand(session, "接听", () -> phoneControlService.answer(session.getFreeswitchUuid()))) {
            return;
        }
        transition(session, CallState.BRIDGED);
        session.setAnswerTime(LocalDateTime.now());
        baseMapper.updateById(session);
        log.info("[PhoneBar] 座席接听: callId={}", callId);
    }

    @Transactional
    public void hangup(String callId, String cause) {
        CallSession session = findSession(callId, "挂断");
        if (session == null) {
            return;
        }
        if (!sendCommand(session, "挂断", () -> phoneControlService.hangup(session.getFreeswitchUuid(), cause))) {
            return;
        }
        transition(session, CallState.ENDED);
        session.setEndTime(LocalDateTime.now());
        session.setEndCause(cause);
        baseMapper.updateById(session);
        log.info("[PhoneBar] 座席挂断: callId={}, cause={}", callId, cause);
    }

    @Transactional
    public void hold(String callId) {
        CallSession session = findSession(callId, "保持");
        if (session == null) {
            return;
        }
        if (!sendCommand(session, "保持", () -> phoneControlService.hold(session.getFreeswitchUuid()))) {
            return;
        }
        transition(session, CallState.HOLD);
        baseMapper.updateById(session);
        log.info("[PhoneBar] 座席保持通话: callId={}", callId);
    }

    @Transactional
    public void unhold(String callId) {
        CallSession session = findSession(callId, "取消保持");
        if (session == null) {
            return;
        }
        if (!sendCommand(session, "取消保持", () -> phoneControlService.unhold(session.getFreeswitchUuid()))) {
            return;
        }
        transition(session, CallState.BRIDGED);
        baseMapper.updateById(session);
        log.info("[PhoneBar] 座席取消保持: callId={}", callId);
    }

    @Transactional
    public void blindTransfer(String callId, String targetNumber) {
        CallSession session = findSession(callId, "盲转");
        if (session == null) {
            return;
        }
        if (!sendCommand(session, "盲转", () -> phoneControlService.blindTransfer(session.getFreeswitchUuid(), targetNumber))) {
            return;
        }
        transition(session, CallState.ENDED);
        session.setEndTime(LocalDateTime.now());
        baseMapper.updateById(session);
        log.info("[PhoneBar] 座席盲转: callId={}, targetNumber={}", callId, targetNumber);
    }

    @Transactional
    public void consultTransfer(String callId, String targetNumber) {
        CallSession session = findSession(callId, "咨询转");
        if (session == null) {
            return;
        }
        if (!sendCommand(session, "咨询转", () -> phoneControlService.consultTransfer(session.getFreeswitchUuid(), targetNumber))) {
            return;
        }
        transition(session, CallState.ENDED);
        session.setEndTime(LocalDateTime.now());
        baseMapper.updateById(session);
        log.info("[PhoneBar] 座席咨询转: callId={}, targetNumber={}", callId, targetNumber);
    }

    public ServiceRecord submitAfterCallWork(ServiceRecord record) {
        return afterCallWorkService.submitAfterCallWork(record);
    }

    public List<CallRecord> listCallRecords(Long agentId) {
        LambdaQueryWrapper<CallRecord> wrapper = new LambdaQueryWrapper<>();
        if (agentId != null) {
            wrapper.eq(CallRecord::getAgentId, agentId);
        }
        wrapper.orderByDesc(CallRecord::getCreatedAt);
        wrapper.last("LIMIT 50");
        return callRecordMapper.selectList(wrapper);
    }

    public List<ServiceRecord> listServiceRecords(String callId) {
        LambdaQueryWrapper<ServiceRecord> wrapper = new LambdaQueryWrapper<>();
        if (callId != null) {
            wrapper.eq(ServiceRecord::getCallId, callId);
        }
        wrapper.orderByDesc(ServiceRecord::getCreatedAt);
        return serviceRecordMapper.selectList(wrapper);
    }

    private CallSession findSession(String callId, String action) {
        CallSession session = baseMapper.selectOne(
                new LambdaQueryWrapper<CallSession>().eq(CallSession::getCallId, callId));
        if (session == null) {
            log.warn("[PhoneBar] {}失败，未找到会话: callId={}", action, callId);
        }
        return session;
    }

    private boolean sendCommand(CallSession session, String action, CommandSupplier supplier) {
        if (session.getFreeswitchUuid() == null || session.getFreeswitchUuid().isBlank()) {
            log.warn("[PhoneBar] 会话缺少 freeswitchUuid，跳过{}命令: callId={}", action, session.getCallId());
            return false;
        }
        PhoneControlCommandService.CommandResult result = supplier.get();
        if (!result.accepted()) {
            log.warn("[PhoneBar] ESL {}命令执行失败: callId={}, error={}", action, session.getCallId(), result.error());
            return false;
        }
        return true;
    }

    private void transition(CallSession session, CallState targetState) {
        CallState currentState = CallState.valueOf(session.getState());
        if (currentState == targetState) {
            return;
        }
        stateMachine.validate(currentState, targetState);
        session.setState(targetState.name());
    }

    @FunctionalInterface
    private interface CommandSupplier {
        PhoneControlCommandService.CommandResult get();
    }
}
