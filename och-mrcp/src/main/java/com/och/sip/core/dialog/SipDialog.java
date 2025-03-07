package com.och.sip.core.dialog;

import com.och.rtp.PortPoolManager;
import com.och.rtp.RtpServer;
import com.och.sip.core.message.SipRequest;
import com.och.sip.core.message.SipResponse;
import lombok.Data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class SipDialog {

    private final String callId;
    private final String localTag;   // 服务端生成的标签（来自200 OK的To头）
    private final String remoteTag;  // 客户端生成的标签（来自INVITE的From头）
    private int localSequence;       // 本地CSeq序列号（移除final）

    private int remoteSequence;      // 远端CSeq序列号（移除final）
    private DialogState state;
    private final Map<String, String> routeSet; // 路由集合
    private String mrcpSessionId; // 关联的MRCP会话ID
    private Integer allocatedRtpPort; // 新增：分配的RTP端口
    private RtpServer rtpServer;
    private String remoteRtpEndpoint; // 远端RTP终结点

    public SipDialog(SipRequest inviteRequest, SipResponse okResponse) {
        this.callId = inviteRequest.getCallId();
        this.localTag = okResponse.getToTag();    // 修正：从To头获取标签
        this.remoteTag = inviteRequest.getFromTag();
        this.localSequence = parseCSeq(inviteRequest.getCSeq());
        this.remoteSequence = 0;  // 初始化远端序列号
        this.state = DialogState.EARLY;
        this.routeSet = parseRouteSet(inviteRequest);
    }

    // 解析Record-Route头字段（示例实现）
    private Map<String, String> parseRouteSet(SipRequest request) {
        LinkedHashMap<String, String> routes = new LinkedHashMap<>();
        String recordRoute = request.getHeader("Record-Route");
        if (recordRoute != null) {
            String[] entries = recordRoute.split(",\\s*");
            for (int i = entries.length - 1; i >= 0; i--) { // 反转顺序
                String uri = entries[i].replaceAll("<|>", "").trim();
                routes.put(uri, uri);
            }
        }
        return Collections.unmodifiableMap(routes);
    }

    // 从CSeq头中提取序列号（如 "123 INVITE" → 123）
    private int parseCSeq(String cseq) {
        if (cseq == null) throw new IllegalArgumentException("Invalid CSeq");
        String[] parts = cseq.split("\\s+");
        return Integer.parseInt(parts[0]);
    }



    public synchronized void updateState(DialogState newState) {
        this.state = newState;
    }

    public void bindMrcpSession(String sessionId) {
        this.mrcpSessionId = sessionId;
    }

    // 绑定端口到Dialog
    public void bindRtpPort(int port) {
        this.allocatedRtpPort = port;
    }

    // 绑定RTP服务器
    public void setRtpServer(RtpServer rtpServer) {
        this.rtpServer = rtpServer;
    }

    // 终止时释放端口和RTP服务器
    public void releaseResources() {
        if (rtpServer != null) {
            rtpServer.stop();
        }
        if (allocatedRtpPort != null) {
            PortPoolManager.getInstance().releasePort(allocatedRtpPort);
        }
    }
}

