package com.och.sip.core.transaction;

import com.och.sip.core.message.SipMessage;
import com.och.sip.core.message.SipRequest;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TransactionManager {

    private static final TransactionManager INSTANCE = new TransactionManager();
    private final ConcurrentHashMap<String, TransactionContext> transactions = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, String> dialogToBranchId = new ConcurrentHashMap<>(); // 新增映射表

    public static TransactionManager getInstance() {
        return INSTANCE;
    }

    public TransactionContext createContext(ChannelHandlerContext ctx, SipRequest request) {
        String branchId = request.getBranchId();
        return transactions.compute(branchId, (k, existingCtx) -> {
            if (existingCtx != null) {
                // 若事务已终止，允许覆盖
                if (existingCtx.getState() == TransactionState.TERMINATED) {
                    existingCtx.cleanup(); // 彻底清理旧事务
                    TransactionContext newCtx = new TransactionContext(ctx, request);
                    newCtx.startTimer();
                    return newCtx;
                } else {
                    // 处理重传
                    existingCtx.handleRetransmission(request);
                    return existingCtx;
                }
            }
            // 创建新事务
            TransactionContext newCtx = new TransactionContext(ctx, request);
            newCtx.startTimer();
            return newCtx;
        });
    }

    public TransactionContext getContextForAck(SipRequest ack) {
        String ackBranchId = ack.getBranchId();
        log.debug("Looking up transaction for ACK branchId: {}", ackBranchId);

        // 1. 通过 ACK 的 branchId 查找
        TransactionContext ctx = transactions.get(ackBranchId);
        if (ctx != null) {
            log.debug("Found transaction by ACK branchId: {}", ackBranchId);
            return ctx;
        }

        // 2. 通过 Dialog 标识查找原始 branchId
        String dialogKey = generateDialogKey(ack);
        log.debug("Falling back to Dialog lookup with key: {}", dialogKey);
        String originalBranchId = dialogToBranchId.get(dialogKey);
        if (originalBranchId != null) {
            log.debug("Found original branchId via Dialog key: {}", originalBranchId);
            return transactions.get(originalBranchId);
        }

        log.warn("No transaction found for ACK branchId or Dialog key");
        return null;
    }


    public String generateDialogKey(SipMessage request) {
        String callId = request.getCallId();
        String fromTag = request.getFromTag();  // 客户端生成的标签（From头）
        String toTag = request.getToTag();      // 服务端生成的标签（To头）
        return String.join("|", callId, fromTag, toTag);
    }

    public void removeContext(String branchId) {
        TransactionContext ctx = transactions.remove(branchId);
        if (ctx != null) {
            ctx.cancelAllTimers();
        }
    }

}
