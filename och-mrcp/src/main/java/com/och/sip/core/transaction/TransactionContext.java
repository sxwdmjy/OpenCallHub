package com.och.sip.core.transaction;

import com.och.config.SipConfig;
import com.och.exception.SdpParseException;
import com.och.sip.core.dialog.DialogManager;
import com.och.sip.core.dialog.DialogState;
import com.och.sip.core.dialog.SipDialog;
import com.och.sip.core.message.SipRequest;
import com.och.sip.core.message.SipResponse;
import com.och.sip.sdp.AudioVideoStrategy;
import com.och.sip.sdp.SdpAnswer;
import com.och.sip.sdp.SdpOffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class TransactionContext {

    @Getter
    private TransactionState state = TransactionState.INIT;
    private final WeakReference<ChannelHandlerContext> channelRef;
    private static final HashedWheelTimer timer = new HashedWheelTimer();
    private final SipRequest originalRequest;
    private volatile SipResponse lastResponse;

    @Getter
    private final String originalBranchId; // 新增字段
    private final EnumMap<TimerType, Timeout> activeTimers = new EnumMap<>(TimerType.class);


    // 定义定时器类型及超时时间（单位：秒）
    enum TimerType {
        TIMER_B(64),    // RFC3261 Timer B (INVITE客户端等待最终响应)
        TIMER_F(64),    // RFC3261 Timer F (非INVITE事务)
        TIMER_D(32),    // RFC3261 Timer D (INVITE服务端等待ACK)
        TIMER_H(32);    // RFC3261 Timer H (INVITE服务端Confirmed状态超时)

        final int duration;

        TimerType(int duration) {
            this.duration = duration;
        }
    }

    public TransactionContext(ChannelHandlerContext ctx, SipRequest request) {
        this.channelRef = new WeakReference<>(ctx);
        this.originalRequest = request;
        this.originalBranchId = request.getBranchId(); // 记录原始branchId
    }

    public synchronized void transitionState(TransactionState newState) {
        if (state.canTransitionTo(newState)) {
            log.info("Transaction {} 状态转换: {} -> {}",
                    originalRequest.getBranchId(), state, newState);
            TransactionState oldState = state;
            state = newState; // 先更新状态
            handleStateChange(oldState, newState); // 再处理状态变更逻辑
        } else {
            log.error("非法状态转换: {} -> {}", state, newState);
            throw new IllegalStateException("Invalid state transition");
        }
    }

    private void handleStateChange(TransactionState oldState, TransactionState newState) {
        switch (newState) {
            case CONFIRMED:
                cancelTimer(TimerType.TIMER_D);
                // 添加：启动定时器迁移到TERMINATED
                scheduleTimer(TimerType.TIMER_H, timeout -> {
                    log.info("Timer H expired, transitioning to TERMINATED");
                    transitionState(TransactionState.TERMINATED);
                });
                break;
            case TERMINATED:
                cleanup(); // 清理资源
                break;
            default:
                // 其他状态处理逻辑
                break;
        }
    }




    private void cancelTimer(TimerType type) {
        Timeout timeout = activeTimers.remove(type);
        if (timeout != null && !timeout.isCancelled()) {
            timeout.cancel();
        }
    }

    public void cancelAllTimers() {
        activeTimers.values().forEach(t -> {
            if (!t.isCancelled()) t.cancel();
        });
        activeTimers.clear();
    }


    protected void cleanup() {
        // 取消所有定时器
        cancelAllTimers();

        // 从事务管理器中移除当前事务
        TransactionManager.getInstance().removeContext(originalRequest.getBranchId());
        String dialogKey = TransactionManager.getInstance().generateDialogKey(lastResponse);
        TransactionManager.getInstance().dialogToBranchId.remove(dialogKey);
        log.info("Transaction {} terminated", originalRequest.getBranchId());
    }


    public void sendProvisionalResponse(int code, String reason) {
        SipResponse response = buildBaseResponse(code, reason);
        response.addHeader("Require", "100rel");
        response.addHeader("To", buildToHeaderWithTag());
        String viaHeader = originalRequest.getHeader("Via");
        if (!viaHeader.contains("branch=z9hG4bK")) {
            viaHeader = viaHeader.replaceFirst("branch=", "branch=z9hG4bK");
        }
        response.addHeader("Via", viaHeader);
        String contactHeader = generateContactHeader();
        response.addHeader("Contact", contactHeader);
        sendResponse(response);
        // 发送临时响应后，事务状态转为PROCEEDING
        transitionState(TransactionState.PROCEEDING);
    }

    public void startTimer() {
        cancelAllTimers(); // 清理旧定时器

        if (originalRequest.isInvite()) {
            // INVITE事务使用Timer B
            scheduleTimer(TimerType.TIMER_B, this::handleInviteTimeout);
        } else {
            // 非INVITE事务使用Timer F
            scheduleTimer(TimerType.TIMER_F, this::handleNonInviteTimeout);
        }
    }

    private void scheduleTimer(TimerType type, Consumer<Timeout> handler) {
        Timeout timeout = timer.newTimeout(handler::accept,
                type.duration,
                TimeUnit.SECONDS);
        activeTimers.put(type, timeout);
    }


    public SipResponse sendFinalResponse(int code, String reason) {
        SipResponse response = buildResponse(code, reason);
        sendResponse(response);

        if (originalRequest.isInvite()) {
            if (code >= 200 && code < 300) {
                transitionState(TransactionState.COMPLETED);
                // 创建 Dialog 并绑定到事务
                SipDialog dialog = DialogManager.getInstance().createDialog(originalRequest, response);
                log.info("Dialog created: {}", dialog.getCallId());
                // 创建 Dialog 并记录映射
                String dialogKey = TransactionManager.getInstance().generateDialogKey(response);
                TransactionManager.getInstance().dialogToBranchId.put(dialogKey, originalBranchId);
            } else {
                transitionState(TransactionState.TERMINATED);
            }
        } else {
            transitionState(TransactionState.TERMINATED);
        }
        return response;
    }


    private void sendResponse(SipResponse response) {
        ChannelHandlerContext ctx = channelRef.get();
        if (ctx == null || !ctx.channel().isActive()) {
            handleTransportError();
            return;
        }
        ctx.writeAndFlush(response);
        lastResponse = response;
    }


    private void handleTransportError() {
        log.error("Channel unavailable for transaction {}", originalRequest.getBranchId());
        sendFinalResponse(503, "Service Unavailable");
    }

    public void handleAck() {
        if (state == TransactionState.COMPLETED) {
            transitionState(TransactionState.CONFIRMED);
        } else {
            log.warn("Received ACK in invalid state: {}", state);
        }
    }

    private SipResponse buildBaseResponse(int code, String reason) {
        // 使用新构造函数创建对象
        SipResponse response = new SipResponse(code, reason);

        // 添加必须的头字段（RFC 3261 §8.2.6）
        response.addHeader("Via", originalRequest.getHeader("Via"));
        response.addHeader("From", originalRequest.getHeader("From"));
        response.addHeader("To", buildToHeaderWithTag());  // 确保To标签存在
        response.addHeader("Call-ID", originalRequest.getHeader("Call-ID"));
        response.addHeader("CSeq", originalRequest.getHeader("CSeq"));

        // 添加Content-Length（如果存在消息体）
        if (response.getBody() != null) {
            response.addHeader("Content-Length",
                    String.valueOf(response.getBody().length()));
        }

        return response;

    }


    private SipResponse buildResponse(int code, String reason) {
        SipResponse response = createBaseResponse(code, reason);
        addStandardHeaders(response);
        handleSdpNegotiation(response, code);
        return response;
    }

    /**
     * 创建基础响应对象
     */
    private SipResponse createBaseResponse(int code, String reason) {
        return new SipResponse(code, reason);
    }

    /**
     * 添加标准SIP协议头
     */
    private void addStandardHeaders(SipResponse response) {
        response.addHeader("Via", originalRequest.getHeader("Via"));
        response.addHeader("From", originalRequest.getHeader("From"));
        response.addHeader("To", buildToHeaderWithTag());
        response.addHeader("Call-ID", originalRequest.getCallId());
        response.addHeader("CSeq", originalRequest.getCSeq());
        String contactHeader = generateContactHeader();
        response.addHeader("Contact", contactHeader);
        response.addHeader("User-Agent", "OCH-SIP/1.0");
        response.addHeader("Accept", "application/sdp");
        response.addHeader("Allow", "INVITE, ACK, CANCEL, OPTIONS, BYE, REGISTER, SUBSCRIBE, NOTIFY, REFER, MESSAGE");
        response.addHeader("Content-Disposition", "session");
    }

    private String generateContactHeader() {
        // 获取服务器地址和传输协议
        String serverAddress = SipConfig.getServerAddress();
        String transport = determineTransportProtocol();
        // 根据协议选择端口
        int port = transport.equals("TCP") ? SipConfig.getTcpPort() : SipConfig.getUdpPort();

        // 构建 Contact 头
        return String.format("<sip:%s:%d;transport=%s>", serverAddress, port, transport);
    }

    private String determineTransportProtocol() {
        ChannelHandlerContext ctx = channelRef.get();
        if (ctx == null) {
            return "TCP"; // 默认协议
        }
        // 根据通道类型判断传输协议
        if (ctx.channel() instanceof NioSocketChannel) {
            return "TCP";
        } else if (ctx.channel() instanceof NioDatagramChannel) {
            return "UDP";
        } else {
            return "TCP"; // 默认协议
        }
    }


    /**
     * 处理SDP协商逻辑
     */
    private void handleSdpNegotiation(SipResponse response, int code) {
        if (code != 200 || StringUtil.isNullOrEmpty(originalRequest.getBody())) {
            return; // 仅对200 OK且存在SDP Offer时处理
        }

        try {
            SdpOffer sdpOffer = parseSdpOffer();
            SdpAnswer sdpAnswer = negotiateSdp(sdpOffer);
            setResponseBody(response, sdpAnswer);
        } catch (SdpParseException e) {
            log.error("SDP negotiation failed: {}", e.getMessage());
            response.setStatusCode(488); // 自动升级为488 Not Acceptable
        }
    }

    /**
     * 构建带服务端标签的To头
     */
    private String buildToHeaderWithTag() {
        String toHeader = originalRequest.getHeader("To");
        // 若 To 头未包含标签，生成并添加服务端标签
        if (!toHeader.contains("tag=")) {
            String serverTag = generateTag();
            toHeader += ";tag=" + serverTag;
        }
        return toHeader;
    }

    private String appendServerTag(String toHeader) {
        return toHeader + ";tag=" + generateTag();
    }

    // ================== 工具方法 ==================

    /**
     * 解析原始请求中的SDP Offer
     */
    private SdpOffer parseSdpOffer() throws SdpParseException {
        SdpOffer sdpOffer = new SdpOffer();
        sdpOffer.parse(originalRequest.getBody());
        return sdpOffer;
    }

    /**
     * 使用策略生成SDP Answer
     */
    private SdpAnswer negotiateSdp(SdpOffer offer) {
        return new AudioVideoStrategy().negotiate(offer);
    }

    /**
     * 设置响应正文并自动添加Content-Length
     */
    private void setResponseBody(SipResponse response, SdpAnswer answer) {
        String sdpBody = answer.toString();
        response.setBody(sdpBody);
        response.addHeader("Content-Type", "application/sdp");
        response.addHeader("Content-Length", String.valueOf(sdpBody.length()));
    }

    private String generateTag() {
        return "sip-tag-" + System.currentTimeMillis();
    }


    public void handleRetransmission(SipRequest request) {
        switch (state) {
            case PROCEEDING:
                // 重传临时响应（1xx）
                sendResponse(lastResponse);
                break;
            case COMPLETED:
                // 仅重传非2xx最终响应（如3xx, 4xx）
                if (lastResponse.getStatusCode() >= 300) {
                    sendResponse(lastResponse);
                }
                break;
            default:
                log.debug("忽略重传请求，当前状态: {}", state);
        }
    }

    private void handleInviteTimeout(Timeout timeout) {
        if (state == TransactionState.PROCEEDING) {
            log.warn("INVITE transaction timeout, sending 408");
            sendFinalResponse(408, "Request Timeout");
        }
    }

    private void handleNonInviteTimeout(Timeout timeout) {
        if (state == TransactionState.PROCEEDING) {
            log.warn("Non-INVITE transaction timeout, sending 408");
            sendFinalResponse(408, "Request Timeout");
        }
    }

}
