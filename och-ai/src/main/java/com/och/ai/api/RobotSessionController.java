package com.och.ai.api;

import com.och.ai.service.impl.RobotFlowEngineImpl;
import com.och.ai.domain.RobotSession;
import com.och.ai.service.IRobotSessionService;
import com.och.common.api.ApiResponse;
import com.och.common.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 机器人会话管理
 * <p>管理 AI 机器人与客户的多轮对话生命周期，包括启动会话、处理轮次和结束会话。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Tag(name = "RobotSession", description = "机器人会话追踪")
@RestController
@RequestMapping("/api/ai/robot-sessions")
@RequiredArgsConstructor
public class RobotSessionController extends BaseController {

    private final IRobotSessionService robotSessionService;

    @Operation(summary = "启动机器人对话")
    @PostMapping("/start")
    public ApiResponse<Map<String, String>> startSession(@RequestParam String callId,
                                                         @RequestParam Long flowId,
                                                         @RequestParam Long versionId) {
        RobotSession session = robotSessionService.startSession(callId, flowId, versionId);
        return success(Map.of(
                "sessionId", session.getSessionId(),
                "state", session.getState()
        ));
    }

    @Operation(summary = "处理一轮对话")
    @PostMapping("/{sessionId}/turn")
    public ApiResponse<RobotFlowEngineImpl.TurnResult> processTurn(
            @PathVariable String sessionId,
            @RequestParam String userText,
            @RequestParam(defaultValue = "INTERNAL") String botType,
            @RequestParam(defaultValue = "你是一个专业的电话客服机器人") String systemPrompt) {
        return success(robotSessionService.processTurn(sessionId, userText, botType, systemPrompt));
    }

    @Operation(summary = "结束机器人对话")
    @PostMapping("/{sessionId}/end")
    public ApiResponse<Void> endSession(@PathVariable String sessionId, @RequestParam String reason) {
        robotSessionService.endSession(sessionId, reason);
        return success();
    }
}
