package com.och.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.och.ai.service.impl.RobotFlowEngineImpl;
import com.och.ai.domain.RobotSession;

/**
 * 机器人会话服务接口
 * <p>统一管理机器人会话的启动、轮次处理和结束，隔离控制层与流程执行引擎。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
public interface IRobotSessionService extends IService<RobotSession> {

    /**
     * 启动机器人会话。
     *
     * @param callId    呼叫 ID
     * @param flowId    机器人流程 ID
     * @param versionId 流程版本 ID
     * @return 机器人会话
     */
    RobotSession startSession(String callId, Long flowId, Long versionId);

    /**
     * 处理一轮机器人对话。
     *
     * @param sessionId    会话 ID
     * @param userText     用户输入文本
     * @param botType      机器人引擎类型
     * @param systemPrompt 系统提示词
     * @return 本轮处理结果
     */
    RobotFlowEngineImpl.TurnResult processTurn(String sessionId, String userText, String botType, String systemPrompt);

    /**
     * 结束机器人会话。
     *
     * @param sessionId 会话 ID
     * @param reason    结束原因
     */
    void endSession(String sessionId, String reason);
}
