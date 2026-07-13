package com.och.calltask.service;
import com.och.ai.service.impl.RobotFlowEngineImpl;
import com.och.calltask.domain.DialResult;
public interface IRobotOutboundService {
    String startRobotCall(Long taskId, Long customerId, String botType, Long flowId, Long versionId, String systemPrompt);
    RobotFlowEngineImpl.TurnResult handleRobotResponse(String sessionId, String userInput, String botType, String systemPrompt);
    void onRobotComplete(String sessionId, DialResult result);
}
