package com.och.mrcp;

import com.och.config.MrcpConfig;
import com.och.engine.*;
import com.och.exception.InvalidGrammarException;
import com.och.exception.InvalidSsmlException;
import com.och.sip.core.codec.MrcpMessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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

    private final String defaultPlatform = "aliyun"; // 可配置
    private final AtomicReference<State> state = new AtomicReference<>(State.INIT);
    private final String sessionId;
    private final Channel channel;
    private Instant lastActivityTime;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final AtomicInteger pendingRequests = new AtomicInteger(0);
    private final AtomicInteger requestIdCounter = new AtomicInteger(0);


    public MrcpSession(String sessionId, String serverIp, int port) {
        this.sessionId = sessionId;
        this.channel = connectToServer(serverIp, port); // 创建并连接Channel
        channel.attr(SESSION_ID_ATTRIBUTE_KEY).set(sessionId); // 绑定会话ID到Channel
        transitionState(State.CONNECTING);
        this.lastActivityTime = Instant.now();
        startSessionTimer();
        channel.closeFuture().addListener(future -> {
            if (state.get() != State.CLOSED) {
                transitionState(State.CLOSED);
            }
        });
    }

    private Channel connectToServer(String serverIp, int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup  workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    socketChannel.pipeline()
                                            .addLast(new MrcpMessageCodec())
                                            .addLast(new MrcpSessionHandler());
                                }
                            });
            return bootstrap.bind(serverIp,port).sync().channel();
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully(); // 释放资源
            workerGroup.shutdownGracefully();
            throw new RuntimeException("Connection interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            bossGroup.shutdownGracefully(); // 释放资源
            workerGroup.shutdownGracefully();
            throw new RuntimeException("Failed to connect to MRCP server: " + e.getMessage(), e);
        }
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
        // 2. 提交到TTS引擎（异步操作）
        CloudConfig config = CloudConfigManager.getConfig(defaultPlatform);
        TtsEngine engine = EngineFactory.getTtsEngine(config.getPlatform());
        engine.synthesize(req.getBody(), (audioData, error) -> {
            if (error != null) {
                sendErrorResponse(req, 500, error.getMessage());
            } else {
                MrcpResponse res = buildSuccessResponse(req);
                channel.writeAndFlush(res);
            }
        }, config);
    }

    /**
     * 处理RECOGNIZE请求（语音识别）
     */
    private void handleRecognizeRequest(MrcpRequest req) {
        CloudConfig config = CloudConfigManager.getConfig(defaultPlatform);
        AsrEngine engine = EngineFactory.getAsrEngine(config.getPlatform());
        engine.recognize(req.getBody().getBytes(), (text, error) -> {
            if (error != null) {
                sendErrorResponse(req, 500, error.getMessage());
            } else {
                MrcpResponse res = buildSuccessResponse(req);
                res.setBody(text);
                channel.writeAndFlush(res);
            }
        }, config);
    }

    /**
     * 构建标准化的 MRCP 成功响应
     *
     * @param req 原始 MRCP 请求对象
     * @return 预置状态码和基础头部的成功响应对象
     */
    private MrcpResponse buildSuccessResponse(MrcpRequest req) {
        MrcpResponse response = new MrcpResponse();
        response.setRequestId(req.getRequestId());
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
            MrcpResponse res = buildSuccessResponse(req);
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
        MrcpResponse res = new MrcpResponse();
        res.setRequestId(req.getRequestId());
        res.setStatusCode(code);
        res.addHeader("Completion-Cause", "002 error");
        res.setBody(reason);
        channel.writeAndFlush(res);
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
     * 验证SSML格式
     */
    private void validateSsml(String ssml) throws InvalidSsmlException {
        if (ssml == null || !ssml.startsWith("<speak>")) {
            throw new InvalidSsmlException("Invalid SSML format");
        }
    }

    /**
     * 处理异步响应（例如来自TTS/ASR引擎的异步结果）
     *
     * @param response MRCP响应消息
     */
    private void processAsyncResponse(MrcpResponse response) {
        try {
            log.debug("Processing async response for request: {}", response.getRequestId());

            // 1. 解析状态码和关键头部
            int statusCode = response.getStatusCode();
            String completionCause = response.getHeaders().getOrDefault("Completion-Cause", "unknown");

            // 2. 根据状态码分派处理逻辑
            switch (statusCode) {
                case 200: // 成功
                    handleSuccessfulAsyncResponse(response, completionCause);
                    break;
                case 500: // 服务器错误
                    handleFailedAsyncResponse(response, "Server error: " + completionCause);
                    break;
                default:
                    log.warn("Unhandled async response status code: {}", statusCode);
            }
        } catch (Exception e) {
            log.error("Failed to process async response: {}", e.getMessage());
            // 可选：发送错误通知或终止会话
            transitionState(State.TERMINATING);
        }
    }

    /**
     * 处理成功的异步响应
     */
    private void handleSuccessfulAsyncResponse(MrcpResponse response, String completionCause) {
        log.info("Async operation succeeded. Request ID: {}, Cause: {}",
                response.getRequestId(), completionCause);

        // 示例：根据 Completion-Cause 执行特定逻辑
        switch (completionCause) {
            case "000 normal":
                // 正常完成，无需额外操作
                break;
            case "001 stop":
                log.info("Operation was stopped by client request.");
                break;
            default:
                log.warn("Unhandled completion cause: {}", completionCause);
        }
    }

    /**
     * 处理失败的异步响应
     */
    private void handleFailedAsyncResponse(MrcpResponse response, String errorMessage) {
        log.error("Async operation failed. Request ID: {}, Error: {}",
                response.getRequestId(), errorMessage);

        // 示例：触发会话终止或重试逻辑
        if (shouldTerminateOnError(response)) {
            transitionState(State.TERMINATING);
        }
    }

    /**
     * 判断是否因错误需要终止会话（可根据业务逻辑扩展）
     */
    private boolean shouldTerminateOnError(MrcpResponse response) {
        // 示例：特定错误码触发终止
        return response.getStatusCode() == 500;
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
            // 发送MRCP协议终止请求（如STOP）
            MrcpRequest stopReq = new MrcpRequest();
            stopReq.setMethod("STOP");
            stopReq.setRequestId(generateRequestId());
            channel.writeAndFlush(stopReq);
            // 关闭底层连接
            transitionState(State.TERMINATING);
            channel.close().addListener(future -> {
                if (future.isSuccess()) {
                    transitionState(State.CLOSED);
                }
            });
        }
    }


    private String generateRequestId() {
        return sessionId + "-" + requestIdCounter.incrementAndGet();
    }
}
