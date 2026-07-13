package com.och.api.service.impl;

import com.och.api.service.ICallService;
import com.och.call.domain.CallSession;
import com.och.call.mapper.CallSessionMapper;
import com.och.esl.application.PhoneControlCommandService;
import com.och.system.domain.query.call.CallQuery;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 呼叫服务实现类
 * <p>处理外呼呼叫的创建等核心逻辑。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallServiceImpl implements ICallService {

    private final PhoneControlCommandService phoneControlService;
    private final CallSessionMapper callSessionMapper;

    @Override
    public Long makeCall(CallQuery query) {
        String callId = UUID.randomUUID().toString();
        log.info("[CallService] 创建外呼呼叫: callId={}, caller={}, callee={}",
                callId, query.getCaller(), query.getCallee());

        // 通过 ESL 发起呼叫
        phoneControlService.originate(query.getCallee(), query.getCaller(), null);

        // 记录呼叫会话
        CallSession session = CallSession.builder()
                .callId(callId)
                .callerNumber(query.getCaller())
                .calledNumber(query.getCallee())
                .direction("OUTBOUND")
                .state("ORIGINATING")
                .build();
        callSessionMapper.insert(session);

        return session.getId();
    }
}
