package com.och.sip.core.dialog;

import com.och.sip.core.message.SipMessage;
import com.och.sip.core.message.SipRequest;
import com.och.sip.core.message.SipResponse;
import com.och.mrcp.MrcpSessionManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DialogManager {
    // 键格式：callId + "|" + localTag + "|" + remoteTag
    private final ConcurrentMap<String, SipDialog> dialogs = new ConcurrentHashMap<>();

    private static final DialogManager INSTANCE = new DialogManager();

    public static DialogManager getInstance() {
        return INSTANCE;
    }

    public void handleAck(SipRequest ackRequest) {
        SipDialog dialog = findDialog(ackRequest);
        if (dialog != null && dialog.getState() == DialogState.EARLY) {
            dialog.updateState(DialogState.CONFIRMED);
            // 根据RFC3261反转Record-Route头
            LinkedHashMap<String, String> routes = new LinkedHashMap<>();
            String recordRoute = ackRequest.getHeader("Record-Route");
            if (recordRoute != null) {
                String[] entries = recordRoute.split(",\\s*");
                for (int i = entries.length - 1; i >= 0; i--) {
                    String uri = entries[i].replaceAll("<|>", "").trim();
                    routes.put(uri, uri);
                }
            }
            dialog.getRouteSet().putAll(routes);
        }
    }

    private Map<String, String> parseRouteSet(SipRequest request) {
        LinkedHashMap<String, String> routes = new LinkedHashMap<>();
        String recordRoute = request.getHeader("Record-Route");
        if (recordRoute != null) {
            // RFC 3261要求反转Record-Route顺序
            String[] entries = recordRoute.split(",\\s*");
            for (int i = entries.length - 1; i >= 0; i--) {
                String uri = entries[i].replaceAll("<|>", "").trim();
                routes.put(uri, uri);
            }
        }
        return Collections.unmodifiableMap(routes);
    }

    public void terminateDialogByCallId(String callId) {
        dialogs.entrySet().removeIf(entry -> entry.getKey().startsWith(callId + "|"));
    }

    // 创建Dialog（原子操作）
    public SipDialog createDialog(SipRequest inviteRequest, SipResponse okResponse) {
        SipDialog dialog = new SipDialog(inviteRequest, okResponse);
        String key = generateKey(dialog);
        dialogs.put(key, dialog);
        return dialog;
    }

    public SipDialog findDialogByCallId(String callId) {
        return dialogs.values().stream()
                .filter(dialog -> dialog.getCallId().equals(callId))
                .findFirst()
                .orElse(null);
    }
    // 查找Dialog（基于消息中的标签）
    public SipDialog findDialog(SipMessage message) {
        String callId = message.getCallId();
        String localTag = message.getToTag();   // 本地标签来自To头（服务端生成）
        String remoteTag = message.getFromTag(); // 远端标签来自From头（客户端生成）
        String key = generateKey(callId, localTag, remoteTag);
        return dialogs.get(key);
    }

    // 终止Dialog
    public void terminateDialog(SipDialog dialog) {
        // 终止前释放端口
        dialog.releaseResources();
        // 终止Dialog前关闭关联的MRCP会话
        if (dialog.getMrcpSessionId() != null) {
            MrcpSessionManager.getInstance().destroySession(dialog.getMrcpSessionId());
        }
        dialogs.remove(generateKey(dialog));
        dialog.updateState(DialogState.TERMINATED);
    }

    // 生成键（格式：callId|localTag|remoteTag）
    private String generateKey(SipDialog dialog) {
        return generateKey(dialog.getCallId(), dialog.getLocalTag(), dialog.getRemoteTag());
    }

    private String generateKey(String callId, String localTag, String remoteTag) {
        return String.join("|", callId, localTag, remoteTag);
    }


}
