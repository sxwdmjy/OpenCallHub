package com.och.sip.transport.handler;

import com.och.sip.core.dialog.DialogManager;
import com.och.sip.core.dialog.SipDialog;
import com.och.sip.core.message.SipMessage;
import com.och.sip.core.message.SipRequest;
import com.och.sip.core.message.SipResponse;
import com.och.sip.core.transaction.TransactionContext;
import com.och.sip.core.transaction.TransactionManager;
import com.och.sip.core.transaction.TransactionState;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionHandler implements SipMessageHandler {
    private final TransactionManager txManager = TransactionManager.getInstance();
    private final DialogManager dialogManager = DialogManager.getInstance();

    @Override
    public void handle(ChannelHandlerContext ctx, SipMessage message, HandlerChain chain) {
        if (message.isRequest()) {
            SipRequest request = (SipRequest) message;
            String method = request.getMethod().toUpperCase();

            // 根据方法类型分派处理逻辑
            switch (method) {
                case "INVITE":
                    SipResponse sipResponse = handleInvite(ctx, request);
                    chain.process(ctx, sipResponse);
                    break;
                case "BYE":
                    handleBye(ctx, request);
                    break;
                case "ACK":
                    handleAck(ctx, request);
                    break;
                default:
                    handleUnsupportedMethod(ctx, request);
                    break;
            }
        }
    }

    // 处理INVITE请求
    private SipResponse handleInvite(ChannelHandlerContext ctx, SipRequest invite) {

        // 创建事务上下文
        TransactionContext txCtx = txManager.createContext(ctx, invite);

        // 发送100 Trying临时响应
        txCtx.sendProvisionalResponse(100, "Trying");

       return txCtx.sendFinalResponse(200, "OK");

    }

    // 处理BYE请求
    private void handleBye(ChannelHandlerContext ctx, SipRequest bye) {
        // 创建 BYE 事务上下文
        TransactionContext context = txManager.createContext(ctx, bye);
        context.startTimer();

        // 查找并终止关联的 Dialog
        SipDialog dialog = dialogManager.findDialog(bye);
        if (dialog != null) {
            dialogManager.terminateDialog(dialog);
            log.info("Dialog {} terminated by BYE request", dialog.getCallId());
            // 发送 200 OK 响应并终止事务
            context.sendFinalResponse(200, "OK");
        } else {
            context.sendFinalResponse(481, "Call Leg/Transaction Does Not Exist");
        }
    }

    // 处理ACK请求
    private void handleAck(ChannelHandlerContext ctx, SipRequest ack) {
        String branchId = ack.getBranchId();
        log.debug("Processing ACK for transaction: {}", branchId);

        // 1. 获取事务上下文（支持 branchId 变更）
        TransactionContext context = txManager.getContextForAck(ack);
        if (context == null) {
            log.error("ACK for non-existent transaction: {}", branchId);
            return;
        }

        // 2. 检查事务状态是否为 COMPLETED
        if (context.getState() != TransactionState.COMPLETED) {
            log.error("Received ACK in invalid state: {}", context.getState());
            return;
        }

        // 3. 处理 ACK 并更新 Dialog 状态
        try {
            context.handleAck();
            log.info("ACK processed for transaction: {}", branchId);
        } catch (Exception e) {
            log.error("Failed to handle ACK: {}", e.getMessage(), e);
        }
    }

    // 处理不支持的方法
    private void handleUnsupportedMethod(ChannelHandlerContext ctx, SipRequest request) {
        SipResponse error = new SipResponse(405, "Method Not Allowed");
        error.addHeader("Allow", "INVITE, ACK, BYE"); // 声明支持的方法
        copyHeaders(request, error);
        ctx.writeAndFlush(error);
    }


    // 复制必要头字段（Via, From, Call-ID, CSeq）
    private void copyHeaders(SipRequest req, SipResponse res) {
        res.addHeader("Via", req.getHeader("Via"));
        res.addHeader("From", req.getHeader("From"));
        res.addHeader("Call-ID", req.getCallId());
        res.addHeader("CSeq", req.getCSeq());
    }



    private void sendErrorResponse(ChannelHandlerContext ctx, SipRequest req, int code, String reason) {
        SipResponse res = new SipResponse(code, reason);
        res.addHeader("Via", req.getHeader("Via"));
        res.addHeader("From", req.getHeader("From"));
        res.addHeader("To", req.getHeader("To"));
        res.addHeader("Call-ID", req.getCallId());
        res.addHeader("CSeq", req.getCSeq());
        ctx.writeAndFlush(res);
    }
}
