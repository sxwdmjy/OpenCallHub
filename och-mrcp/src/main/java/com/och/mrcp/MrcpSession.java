package com.och.mrcp;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.och.config.MrcpConfig;
import com.och.engine.*;
import com.och.exception.InvalidGrammarException;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Data
public class MrcpSession {

    public static final AttributeKey<String> SESSION_ID_ATTRIBUTE_KEY = AttributeKey.valueOf("sessionId");
    public enum State {INIT, CONNECTING, READY, ACTIVE, TERMINATING, CLOSED}

    private final AtomicReference<State> state = new AtomicReference<>(State.INIT);
    private final String sessionId;
    private Instant lastActivityTime;
    private Channel channel;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final AtomicInteger pendingRequests = new AtomicInteger(0);
    private final AtomicInteger requestIdCounter = new AtomicInteger(0);


    public MrcpSession(String sessionId) {
        this.sessionId = sessionId;
        transitionState(State.CONNECTING);
        this.lastActivityTime = Instant.now();
        startSessionTimer();
    }


    public void process(MrcpMessage msg) {
        State currentState = state.get();
        log.trace("Processing message in state {}: {}", currentState, msg.getClass().getSimpleName());

        try {
            switch (currentState) {
                case READY:
                    handleReadyState(msg);
                    break;
                case ACTIVE:
                    handleActiveState(msg);
                    break;
                case TERMINATING:
                    handleTerminatingState(msg);
                    break;
                case INIT:
                case CONNECTING:
                case CLOSED:
                    log.warn("Received message in invalid state: {}", currentState);
                    sendErrorResponseIfRequest(msg, 481, "Session Not Ready");
                    break;
                default:
                    log.error("Unknown state: {}", currentState);
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
            sendErrorResponseIfRequest(msg, 500, "Internal Server Error");
        }
    }


    /**
     * 如果是请求消息，发送错误响应
     */
    private void sendErrorResponseIfRequest(MrcpMessage msg, int code, String reason) {
        if (msg instanceof MrcpRequest) {
            MrcpRequest req = (MrcpRequest) msg;
            sendErrorResponse(req, code, reason);
        }
    }

    // ================= 核心状态处理方法 =================

    /**
     * 处理READY状态消息（会话已建立，等待操作）
     */
    private void handleReadyState(MrcpMessage msg) {
        if (msg instanceof MrcpResponse) {
            MrcpResponse res = (MrcpResponse) msg;
            if (res.getStatusCode() == 200) {
                log.info("Session {} ready to process requests", sessionId);
                transitionState(State.ACTIVE);
            } else {
                log.error("Session {} setup failed: {}", sessionId, res.getStatusCode());
                transitionState(State.TERMINATING);
            }
        }
    }

    /**
     * 处理ACTIVE状态消息（处理媒体操作请求）
     */
    private void handleActiveState(MrcpMessage msg) {
        this.lastActivityTime = Instant.now();
        if (msg instanceof MrcpRequest) {
            MrcpRequest req = (MrcpRequest) msg;
            pendingRequests.incrementAndGet();

            try {
                switch (req.getMethod().toUpperCase()) {
                    case "SPEAK":
                        handleSpeakRequest(req);
                        break;
                    case "RECOGNIZE":
                        handleRecognizeRequest(req);
                        break;
                    case "STOP":
                        handleStopRequest(req);
                        break;
                    // 新增 DEFINE-GRAMMAR 处理分支
                    case "DEFINE-GRAMMAR":
                        handleDefineGrammarRequest(req);
                        break;
                    default:
                        sendErrorResponse(req, 405, "Method Not Allowed");
                }
            } finally {
                pendingRequests.decrementAndGet();
            }
        } else if (msg instanceof MrcpResponse) {
            processAsyncResponse((MrcpResponse) msg);
        }
    }

    /**
     * 处理TERMINATING状态消息（清理资源）
     */
    private void handleTerminatingState(MrcpMessage msg) {
        if (msg instanceof MrcpRequest) {
            // 拒绝新请求
            sendErrorResponse((MrcpRequest) msg, 481, "Session Terminating");
        }

        if (pendingRequests.get() == 0) {
            channel.close().addListener(future -> transitionState(State.CLOSED));
        }

    }

    // ================= 具体请求处理方法 =================

    /**
     * 处理SPEAK请求（语音合成）
     */
    private void handleSpeakRequest(MrcpRequest req) {
        log.trace("Processing SPEAK request: {}", req.getRequestId());
    }

    /**
     * 处理RECOGNIZE请求（语音识别）
     */
    private void handleRecognizeRequest(MrcpRequest req) {
        log.trace("Processing RECOGNIZE request: {}", req.getRequestId());
        AsrEngine asrEngine = EngineFactory.getAsrEngine("aliyun");
        asrEngine.start(req,this);
        EngineFactory.addMrcpAsrEngine(sessionId, asrEngine);
        // 1. 提交到ASR引擎（异步操作）
        MrcpResponse res = buildSuccessResponse(req,"IN-PROGRESS");
        channel.writeAndFlush(res);

    }

    /**
     * 构建标准化的 MRCP 成功响应
     *
     * @param req 原始 MRCP 请求对象
     * @return 预置状态码和基础头部的成功响应对象
     */
    private MrcpResponse buildSuccessResponse(MrcpRequest req, String statusText) {
        MrcpResponse response = new MrcpResponse();
        response.setVersion(req.getVersion());
        response.setMessageLength(-1);
        response.setRequestId(req.getRequestId());
        response.setStatusText(statusText);
        response.setStatusCode(200);
        response.addHeader("Completion-Cause", "000 normal");
        response.addHeader("Channel-Identifier", this.sessionId);
        return response;
    }


    /**
     * 处理STOP请求（终止当前操作）
     */
    private void handleStopRequest(MrcpRequest req) {
        MrcpResponse res = new MrcpResponse();
        res.setVersion(req.getVersion());
        res.setRequestId(req.getRequestId());
        res.setStatusCode(200);
        res.addHeader("Completion-Cause", "001 stop");
        channel.writeAndFlush(res);
    }

    /**
     * 处理 DEFINE-GRAMMAR 请求
     */
    private void handleDefineGrammarRequest(MrcpRequest req) {
        log.info("Processing DEFINE-GRAMMAR request: {}", req.getRequestId());

        try {
            // 1. 解析语法内容（SRGS XML）
            String grammarXml = req.getBody();
            validateGrammar(grammarXml); // 语法验证逻辑

            // 2. 存储语法（示例：关联到当前会话）
            String grammarId = req.getHeader("Content-Id");
            if (grammarId == null) {
                grammarId = "grammar-" + System.currentTimeMillis();
            }
            GrammarManager.storeGrammar(sessionId, grammarId, grammarXml);

            // 3. 发送成功响应
            MrcpResponse res = buildSuccessResponse(req,"COMPLETE");
            res.addHeader("Content-Id", grammarId); // 返回存储的语法ID
            channel.writeAndFlush(res);
        } catch (InvalidGrammarException e) {
            log.error("Invalid grammar: {}", e.getMessage());
            sendErrorResponse(req, 400, "Bad Request: Invalid Grammar");
        } catch (Exception e) {
            log.error("Failed to process DEFINE-GRAMMAR: {}", e.getMessage());
            sendErrorResponse(req, 500, "Internal Server Error");
        }
    }


    /**
     * 验证语法格式（示例：简单检查是否为有效 XML）
     */
    private void validateGrammar(String grammarXml) throws InvalidGrammarException {
        if (grammarXml == null || !grammarXml.contains("<grammar")) {
            throw new InvalidGrammarException("Invalid SRGS grammar format");
        }
    }

    // ================= 工具方法 =================

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(MrcpRequest req, int code, String reason) {
        MrcpResponse response = new MrcpResponse();
        response.setVersion(req.getVersion());
        response.setMessageLength(reason.length());
        response.setRequestId(req.getRequestId());
        response.setStatusText("COMPLETE");
        response.setStatusCode(code);
        response.addHeader("Completion-Cause", "002 error");
        response.addHeader("Channel-Identifier", this.sessionId);
        response.addHeader("Content-Length", String.valueOf(reason.length()));
        response.setBody(reason);
        channel.writeAndFlush(response);
    }

    /**
     * 状态转换（线程安全）
     */
    protected synchronized void transitionState(State newState) {
        log.debug("Session {} state transition: {} -> {}", sessionId, state, newState);
        state.set(newState);
        if (newState == State.TERMINATING) {
            // 触发终止流程
            handleTerminatingState(null);
        }
    }


    /**
     * 处理异步响应（例如来自TTS/ASR引擎的异步结果）
     *
     * @param response MRCP响应消息
     */
    private void processAsyncResponse(MrcpResponse response) {
        channel.writeAndFlush(response);
        EngineFactory.getMrcpAsrEngine(sessionId).end();
    }


    private void startSessionTimer() {
        new Thread(() -> {
            while (true) {
                if (Duration.between(lastActivityTime, Instant.now()).compareTo(MrcpConfig.getSessionTimeout()) > 0) {
                    close();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void close() {
        if (state.get() != State.CLOSED) {
            transitionState(State.CLOSED);
        }
    }

}
