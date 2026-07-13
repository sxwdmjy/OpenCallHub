package com.och.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.ai.application.adapter.BotAdapter;
import com.och.ai.application.adapter.PluginAdapterFactory;
import com.och.ai.domain.AiConversation;
import com.och.ai.domain.AiConversationTurn;
import com.och.ai.domain.PluginCallLog;
import com.och.ai.domain.RobotSession;
import com.och.ai.mapper.AiConversationMapper;
import com.och.ai.mapper.AiConversationTurnMapper;
import com.och.ai.mapper.PluginCallLogMapper;
import com.och.ai.mapper.RobotSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 机器人流程执行引擎
 * <p>
 * 负责机器人实时对话编排：选择 AI 适配器、维护最近几轮上下文、判断是否转人工，
 * 并把会话、轮次和插件调用日志写入数据库，方便后续轨迹回放、成本统计和问题排查。
 * </p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>适配器模式</b>：BotAdapter 屏蔽 Dify、RAGFlow、LangFlow、内置机器人差异</li>
 *   <li><b>工厂模式</b>：PluginAdapterFactory 根据 botType 选择适配器实现</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotFlowEngineImpl {

    private final PluginAdapterFactory adapterFactory;
    private final RobotSessionMapper robotSessionMapper;
    private final AiConversationMapper aiConversationMapper;
    private final AiConversationTurnMapper aiConversationTurnMapper;
    private final PluginCallLogMapper pluginCallLogMapper;

    /**
     * 实时对话上下文缓存。
     * <p>这里保存最近几轮对话，便于低延迟构造上下文；数据库记录负责审计和轨迹回放。</p>
     */
    private final ConcurrentHashMap<String, RobotSessionContext> activeSessions = new ConcurrentHashMap<>();

    /**
     * 发起新的机器人对话。
     *
     * @param callId    呼叫 ID
     * @param flowId    机器人流程 ID
     * @param versionId 流程版本 ID
     * @return 机器人会话
     */
    @Transactional
    public RobotSession startSession(String callId, Long flowId, Long versionId) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        RobotSession session = new RobotSession();
        session.setCallId(callId);
        session.setSessionId(sessionId);
        session.setFlowId(flowId);
        session.setVersionId(versionId);
        session.setState("ACTIVE");
        session.setTurnCount(0);
        session.setTotalAsrLatencyMs(0);
        session.setTotalTtsLatencyMs(0);
        session.setTotalLlmLatencyMs(0);
        session.setCreatedAt(now);
        robotSessionMapper.insert(session);

        AiConversation conversation = new AiConversation();
        conversation.setCallId(callId);
        conversation.setSessionId(sessionId);
        conversation.setState("ACTIVE");
        conversation.setTurnCount(0);
        conversation.setCreatedAt(now);
        aiConversationMapper.insert(conversation);

        activeSessions.put(sessionId, new RobotSessionContext(callId, session.getId(), conversation.getId(), 0, new ArrayList<>()));
        log.info("[AI机器人] 会话启动: sessionId={}, callId={}, flowId={}, versionId={}", sessionId, callId, flowId, versionId);
        return session;
    }

    /**
     * 处理一轮机器人对话。
     *
     * @param sessionId    会话 ID
     * @param userText     用户语音识别后的文本
     * @param botType      机器人引擎类型：DIFY/RAGFLOW/LANGFLOW/INTERNAL
     * @param systemPrompt 系统提示词
     * @return AI 回复、意图、是否转人工和延迟
     */
    @Transactional
    public TurnResult processTurn(String sessionId, String userText, String botType, String systemPrompt) {
        RobotSessionContext ctx = activeSessions.computeIfAbsent(sessionId, this::loadContext);
        if (ctx == null) {
            log.warn("[AI机器人] 会话不存在或已过期: sessionId={}", sessionId);
            return new TurnResult("会话已过期", "EXPIRED", false, 0);
        }

        int turnNo = ctx.turnCount + 1;
        String context = buildContext(ctx.history, 3);
        String pluginType = normalizeBotType(botType);

        try {
            BotAdapter adapter = adapterFactory.getAdapter(pluginType);
            BotAdapter.BotResponse botResp = adapter.process(userText, context, systemPrompt, turnNo);
            boolean needTransfer = checkTransferCondition(botResp.intent(), botResp.text());

            saveTurnTrace(sessionId, ctx, turnNo, userText, pluginType, botResp, true, null);
            updateActiveContext(ctx, turnNo, userText, botResp);

            log.info("[AI机器人] 第{}轮完成: sessionId={}, intent={}, transfer={}, latency={}ms",
                    turnNo, sessionId, botResp.intent(), needTransfer, botResp.latencyMs());
            return new TurnResult(botResp.text(), botResp.intent(), needTransfer, botResp.latencyMs());
        } catch (Exception e) {
            BotAdapter.BotResponse failed = new BotAdapter.BotResponse("AI 服务暂时不可用，请稍后再试。", "ERROR", "{}", 0);
            String errorSummary = buildErrorSummary(e);
            saveTurnTrace(sessionId, ctx, turnNo, userText, pluginType, failed, false, errorSummary);
            updateActiveContext(ctx, turnNo, userText, failed);
            log.error("[AI机器人] 第{}轮处理失败: sessionId={}, errorType={}", turnNo, sessionId, errorSummary);
            return new TurnResult(failed.text(), failed.intent(), true, failed.latencyMs());
        }
    }

    @Transactional
    public void endSession(String sessionId, String reason) {
        RobotSessionContext ctx = activeSessions.remove(sessionId);
        LocalDateTime now = LocalDateTime.now();

        RobotSession session = selectSession(sessionId);
        if (session != null) {
            session.setState("ENDED");
            session.setEndedAt(now);
            session.setEndReason(reason);
            robotSessionMapper.updateById(session);
        }

        AiConversation conversation = selectConversation(sessionId);
        if (conversation != null) {
            conversation.setState("ENDED");
            conversation.setEndedAt(now);
            if (ctx != null) {
                conversation.setTurnCount(ctx.turnCount);
            }
            aiConversationMapper.updateById(conversation);
        }

        log.info("[AI机器人] 会话结束: sessionId={}, reason={}", sessionId, reason);
    }

    private RobotSessionContext loadContext(String sessionId) {
        RobotSession session = selectSession(sessionId);
        AiConversation conversation = selectConversation(sessionId);
        if (session == null || conversation == null || !"ACTIVE".equals(session.getState())) {
            return null;
        }
        return new RobotSessionContext(
                session.getCallId(),
                session.getId(),
                conversation.getId(),
                conversation.getTurnCount() == null ? 0 : conversation.getTurnCount(),
                new ArrayList<>()
        );
    }

    private void saveTurnTrace(String sessionId, RobotSessionContext ctx, int turnNo, String userText,
                               String pluginType, BotAdapter.BotResponse botResp, boolean success, String errorMessage) {
        if (ctx.conversationId != null) {
            AiConversationTurn turn = new AiConversationTurn();
            turn.setConversationId(ctx.conversationId);
            turn.setTurnNo(turnNo);
            turn.setUserText(userText);
            turn.setAiResponse(botResp.text());
            turn.setIntent(botResp.intent());
            turn.setSlotValuesJson(botResp.slotValuesJson());
            turn.setAsrLatencyMs(0);
            turn.setTtsLatencyMs(0);
            turn.setLlmLatencyMs(botResp.latencyMs());
            turn.setCreatedAt(LocalDateTime.now());
            aiConversationTurnMapper.insert(turn);
        }

        PluginCallLog callLog = new PluginCallLog();
        callLog.setSessionId(sessionId);
        callLog.setCallId(ctx.callId);
        callLog.setPluginType("WORKFLOW");
        callLog.setPluginCode(pluginType);
        callLog.setTurnNo(turnNo);
        callLog.setRequestData(buildRequestSummary(userText));
        callLog.setResponseData(buildResponseSummary(botResp.text(), botResp.intent()));
        callLog.setLatencyMs(botResp.latencyMs());
        callLog.setSuccess(success);
        callLog.setErrorMessage(trim(errorMessage, 500));
        callLog.setCost(0D);
        callLog.setCreatedAt(LocalDateTime.now());
        pluginCallLogMapper.insert(callLog);

        RobotSession session = ctx.sessionId == null ? selectSession(sessionId) : robotSessionMapper.selectById(ctx.sessionId);
        if (session != null) {
            session.setCurrentIntent(botResp.intent());
            session.setSlotValuesJson(botResp.slotValuesJson());
            session.setTurnCount(turnNo);
            session.setTotalLlmLatencyMs((session.getTotalLlmLatencyMs() == null ? 0 : session.getTotalLlmLatencyMs()) + botResp.latencyMs());
            robotSessionMapper.updateById(session);
        }

        AiConversation conversation = ctx.conversationId == null ? selectConversation(sessionId) : aiConversationMapper.selectById(ctx.conversationId);
        if (conversation != null) {
            conversation.setLlmPluginCode(pluginType);
            conversation.setTurnCount(turnNo);
            aiConversationMapper.updateById(conversation);
        }
    }

    private void updateActiveContext(RobotSessionContext ctx, int turnNo, String userText, BotAdapter.BotResponse botResp) {
        ctx.turnCount = turnNo;
        ctx.history.add(new TurnRecord(turnNo, userText, botResp.text(), botResp.intent()));
    }

    private RobotSession selectSession(String sessionId) {
        return robotSessionMapper.selectOne(
                new LambdaQueryWrapper<RobotSession>()
                        .eq(RobotSession::getSessionId, sessionId)
                        .last("LIMIT 1")
        );
    }

    private AiConversation selectConversation(String sessionId) {
        return aiConversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getSessionId, sessionId)
                        .last("LIMIT 1")
        );
    }

    private boolean checkTransferCondition(String intent, String text) {
        return intent != null && (intent.startsWith("TRANSFER") || intent.equals("EXPIRED") || intent.equals("ERROR"));
    }

    private String buildContext(List<TurnRecord> history, int maxTurns) {
        if (history.isEmpty()) {
            return "";
        }
        int from = Math.max(0, history.size() - maxTurns);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < history.size(); i++) {
            TurnRecord t = history.get(i);
            sb.append("用户: ").append(t.userText).append("\n机器人: ").append(t.botText).append("\n");
        }
        return sb.toString();
    }

    private String normalizeBotType(String botType) {
        return botType == null || botType.isBlank() ? "INTERNAL" : botType.trim().toUpperCase();
    }

    private String buildRequestSummary(String userText) {
        return "{\"userTextLength\":" + (userText == null ? 0 : userText.length()) + "}";
    }

    private String buildResponseSummary(String text, String intent) {
        return "{\"intent\":\"" + escapeJson(intent) + "\",\"textLength\":" + (text == null ? 0 : text.length()) + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String buildErrorSummary(Exception e) {
        return e == null ? "UNKNOWN_ERROR" : e.getClass().getSimpleName();
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public record TurnResult(String text, String intent, boolean needTransfer, int latencyMs) {}

    private static class RobotSessionContext {
        final String callId;
        final Long sessionId;
        final Long conversationId;
        int turnCount;
        final List<TurnRecord> history;

        RobotSessionContext(String callId, Long sessionId, Long conversationId, int turnCount, List<TurnRecord> history) {
            this.callId = callId;
            this.sessionId = sessionId;
            this.conversationId = conversationId;
            this.turnCount = turnCount;
            this.history = history;
        }
    }

    private record TurnRecord(int turnNo, String userText, String botText, String intent) {}
}
