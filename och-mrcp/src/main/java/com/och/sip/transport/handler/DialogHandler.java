package com.och.sip.transport.handler;

import com.och.sip.core.dialog.DialogManager;
import com.och.sip.core.dialog.SipDialog;
import com.och.sip.core.message.SipMessage;
import io.netty.channel.ChannelHandlerContext;

public class DialogHandler implements SipMessageHandler {
    private final DialogManager dialogManager = new DialogManager();

    @Override
    public void handle(ChannelHandlerContext ctx, SipMessage message, HandlerChain chain) {
        if (message.isRequest() && "BYE".equals(message.getMethod())) {
            // 处理BYE请求，终止Dialog
            SipDialog dialog = dialogManager.findDialog(message);
            if (dialog != null) {
                dialogManager.terminateDialog(dialog);
            }
        }
        chain.process(ctx, message);
    }
}
