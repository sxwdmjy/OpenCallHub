package com.och.esl.application;

import com.och.esl.application.EslConnectionManager;
import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ESL 入站事件监听器
 * <p>接收 FreeSWITCH 推送的原始 ESL 事件，经过规范化后<strong>异步发布</strong>为 Spring
 * ApplicationEvent，供下游模块（och-call）通过 {@code @EventListener} 消费。</p>
 *
 * <p>异步发布通过 {@link EslEventAsyncPublisher} 实现：
 * 使用 {@link com.och.esl.config.EslEventAsyncConfig} 配置的专用线程池（核心4/最大16），
 * 并通过 callId 哈希分区保证同一通话的事件顺序处理。</p>
 *
 * <h3>事件处理流程</h3>
 * <ol>
 *   <li>FreeSWITCH 推送原始 ESL 事件 → onEslEvent(Map headers)</li>
 *   <li>通过 EslEventNormalizer 规范化为 NormalizedEslEvent</li>
 *   <li>HEARTBEAT → 更新连接管理器心跳时间</li>
 *   <li>DTMF → 推入 DtmfProcessingService 缓冲区</li>
 *   <li>通过 EslEventAsyncPublisher 异步发布 Spring 事件供下游消费</li>
 * </ol>
 *
 * <h3>事件过滤规则</h3>
 * <p>仅处理以下通道事件，CUSTOM 事件根据配置决定是否发布：</p>
 * <ul>
 *   <li>通道生命周期事件：CREATE / ANSWER / BRIDGE / HANGUP / DESTROY</li>
 *   <li>媒体事件：DTMF / RECORD_START / RECORD_STOP</li>
 *   <li>状态事件：HOLD / UNHOLD / PROGRESS</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Service
public class EslEventListener{

    private final EslEventNormalizer normalizer;
    private final EslEventAsyncPublisher asyncPublisher;
    private final EslConnectionManager connectionManager;
    private final DtmfProcessingService dtmfService;

    public EslEventListener(EslEventNormalizer normalizer,
                            EslEventAsyncPublisher asyncPublisher,
                            EslConnectionManager connectionManager,
                            DtmfProcessingService dtmfService) {
        this.normalizer = normalizer;
        this.asyncPublisher = asyncPublisher;
        this.connectionManager = connectionManager;
        this.dtmfService = dtmfService;
    }

    /**
     * 接收 FreeSWITCH 原始 ESL 事件
     * <p>这是 ESL 客户端回调的入口方法，每个 FreeSWITCH 推送的事件都会进入此方法。
     * 生产环境下由 FreeSWITCH ESL client 库回调。</p>
     *
     * @param rawHeaders FreeSWITCH 原始事件头键值对
     */
    public void onEslEvent(Map<String, String> rawHeaders) {
        NormalizedEslEvent event = normalizer.normalize(rawHeaders);
        EslEventType type = event.getEventType();

        // ── 心跳：更新连接管理器心跳时间，维持连接健康检查 ──
        if (type == EslEventType.HEARTBEAT) {
            String fsId = extractFsId(event);  // 从事件中解析 FreeSWITCH 节点标识
            connectionManager.updateHeartbeat(fsId != null ? fsId : "default");
            log.debug("[EslEventListener] 收到心跳: uuid={}", event.getFreeswitchUuid());
            return;
        }

        if (event.getCallId() == null || event.getCallId().isBlank()) {
            log.debug("[EslEventListener] 事件缺少 callId，忽略发布: type={}, uuid={}", type, event.getFreeswitchUuid());
            return;
        }

        // ── DTMF：推入缓冲区供 IVR 按键导航使用 ──
        if (type == EslEventType.DTMF && event.getDtmfDigit() != null) {
            String digits = dtmfService.pushDigit(event.getCallId(), event.getDtmfDigit(), 30);
            log.debug("[EslEventListener] DTMF按键: callId={}, digit={}, sequence={}",
                    event.getCallId(), event.getDtmfDigit(), digits);
            // DTMF 事件仍继续发布，供 IVR 等下游模块监听
        }

        // ── 自定义事件：仅记录不发布 ──
        if (type == EslEventType.CUSTOM) {
            log.debug("[EslEventListener] 收到自定义事件: uuid={}", event.getFreeswitchUuid());
            return;
        }

        // ── 关键事件记录（号码脱敏） ──
        switch (type) {
            case CHANNEL_CREATE, CHANNEL_OUTGOING, CHANNEL_ANSWER, CHANNEL_BRIDGE,
                 CHANNEL_UNBRIDGE, CHANNEL_HANGUP, CHANNEL_DESTROY ->
                log.info("[EslEventListener] 关键事件: type={}, callId={}, uuid={}, caller={}, called={}",
                    type, event.getCallId(), event.getFreeswitchUuid(),
                    EslUtils.maskPhone(event.getCallerNumber()), EslUtils.maskPhone(event.getCalledNumber()));
            default ->
                log.debug("[EslEventListener] 事件: type={}, uuid={}", type, event.getFreeswitchUuid());
        }

        // ── 异步发布 Spring 事件供下游消费 ──
        asyncPublisher.asyncPublish(event);
    }

    /**
     * 从事件中提取 FreeSWITCH 节点标识
     * <p>通过 channelVars 中的 FreeSWITCH 主机名或 IP 来标识节点。
     * 生产环境可在 ESL 连接建立时将 freeswitchId 注入通道变量。</p>
     */
    private String extractFsId(NormalizedEslEvent event) {
        if (event.getChannelVars() != null) {
            // 尝试从通道变量中获取
            String hostname = event.getChannelVars().get("hostname");
            if (hostname != null) return hostname;
            String localIp = event.getChannelVars().get("local_ip_v4");
            if (localIp != null) return localIp;
        }
        // fallback: 使用 rawHeaders 中的 FreeSWITCH-IPv4
        if (event.getRawHeaders() != null) {
            String fsIp = event.getRawHeaders().get("FreeSWITCH-IPv4");
            if (fsIp != null) return fsIp;
        }
        return null;
    }

}
