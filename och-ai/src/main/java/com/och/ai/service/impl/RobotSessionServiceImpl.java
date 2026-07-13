package com.och.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.och.ai.service.impl.RobotFlowEngineImpl;
import com.och.ai.domain.RobotSession;
import com.och.ai.mapper.RobotSessionMapper;
import com.och.ai.service.IRobotSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 机器人会话服务实现
 * <p>作为 Controller 与流程执行引擎之间的门面，保持控制层简洁，具体会话追踪由引擎统一落库。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Service
@RequiredArgsConstructor
public class RobotSessionServiceImpl extends ServiceImpl<RobotSessionMapper, RobotSession> implements IRobotSessionService {

    private final RobotFlowEngineImpl robotFlowEngine;

    @Override
    public RobotSession startSession(String callId, Long flowId, Long versionId) {
        return robotFlowEngine.startSession(callId, flowId, versionId);
    }

    @Override
    public RobotFlowEngineImpl.TurnResult processTurn(String sessionId, String userText, String botType, String systemPrompt) {
        return robotFlowEngine.processTurn(sessionId, userText, botType, systemPrompt);
    }

    @Override
    public void endSession(String sessionId, String reason) {
        robotFlowEngine.endSession(sessionId, reason);
    }
}
