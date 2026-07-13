package com.och.esl.application;

import com.och.esl.config.EslConfigurationProperties;
import com.och.esl.application.EslEventListener;
import lombok.extern.slf4j.Slf4j;
import com.och.esl.application.EslEventListener;
import lombok.RequiredArgsConstructor;
import com.och.esl.application.EslEventListener;
import org.springframework.beans.factory.ObjectProvider;
import com.och.esl.application.EslEventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ESL 连接管理器。
 * <p>
 * 管理 FreeSWITCH ESL 连接生命周期、心跳状态、重连开关和连接统计。
 * 真实 TCP 连接由 {@link EslConnectionClient} 适配器完成，本类只维护业务侧可见的连接状态，
 * 避免连接管理逻辑直接绑定某一个 ESL SDK。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EslConnectionManager {

    /** 连接状态记录 */
    public enum ConnectionState {
        /** 未连接 */
        DISCONNECTED,
        /** 正在 TCP 连接中 */
        CONNECTING,
        /** TCP 连接已建立，正在进行 ESL 认证 */
        AUTHENTICATING,
        /** 已认证，连接正常 */
        CONNECTED,
        /** 正在重连 */
        RECONNECTING,
        /** 连接失败 */
        FAILED,
        /** 连接降级，通常表示心跳超时但尚未确认断开 */
        DEGRADED
    }

    /**
     * ESL 连接统计信息。
     */
    public record ConnectionStats(
            String freeswitchId,
            String host,
            int port,
            ConnectionState state,
            int connectCount,
            int disconnectCount,
            LocalDateTime lastConnectTime,
            LocalDateTime lastDisconnectTime,
            LocalDateTime lastHeartbeat,
            long uptimeSeconds
    ) {}

    /** 连接配置，断线重连时复用。 */
    private record ConnectionConfig(String host, int port, String password) {}

    private final EslConnectionClient connectionClient;
    private final EslConfigurationProperties config;
    private final ObjectProvider<EslEventListener> eventListenerProvider;

    /** 各 FreeSWITCH 节点的连接状态 */
    private final Map<String, ConnectionState> connectionStates = new ConcurrentHashMap<>();

    /** 连接统计信息 */
    private final Map<String, ConnectionStats> connectionStatsMap = new ConcurrentHashMap<>();

    /** 重连计数 */
    private final Map<String, AtomicInteger> reconnectCounters = new ConcurrentHashMap<>();

    /** 重连开关 */
    private final Map<String, AtomicBoolean> reconnectEnabled = new ConcurrentHashMap<>();

    /** 各节点最后心跳时间 */
    private final Map<String, LocalDateTime> lastHeartbeatTimes = new ConcurrentHashMap<>();

    /** 连接配置缓存 */
    private final Map<String, ConnectionConfig> configCache = new ConcurrentHashMap<>();

    /**
     * 连接到 FreeSWITCH 实例。
     * <p>
     * 连接成功只代表 ESL 连接被适配器接受；真实通话事件仍以 HEARTBEAT、CHANNEL_* 等事件为准。
     * </p>
     */
    public void connect(String freeswitchId, String host, int port, String password) {
        validateConnectionArgs(freeswitchId, host, port);
        connectionStates.put(freeswitchId, ConnectionState.CONNECTING);
        configCache.put(freeswitchId, new ConnectionConfig(host, port, password));
        initStatsIfAbsent(freeswitchId, host, port);
        log.info("[EslConnection] 正在连接 FreeSWITCH: id={}, host={}:{}", freeswitchId, host, port);

        try {
            connectionStates.put(freeswitchId, ConnectionState.AUTHENTICATING);
            EslConnectionClient.ConnectionRequest request = new EslConnectionClient.ConnectionRequest(
                    freeswitchId, host, port, password, config.getConnectTimeoutMs());
            EslConnectionClient.ConnectionResult result = connectionClient.connect(request, headers -> {
                EslEventListener listener = eventListenerProvider.getIfAvailable();
                if (listener != null) {
                    listener.onEslEvent(headers);
                }
            });

            if (result.connected()) {
                markConnected(freeswitchId, host, port);
                log.info("[EslConnection] FreeSWITCH 连接成功: id={}, host={}:{}", freeswitchId, host, port);
                return;
            }

            markFailed(freeswitchId);
            log.warn("[EslConnection] FreeSWITCH 连接被拒绝: id={}, host={}:{}, error={}",
                    freeswitchId, host, port, result.error());
        } catch (Exception e) {
            markFailed(freeswitchId);
            log.error("[EslConnection] FreeSWITCH 连接失败: id={}, host={}:{}", freeswitchId, host, port, e);
        }
    }

    /**
     * 标记连接降级。
     */
    public void markDegraded(String freeswitchId) {
        if (!isKnownNode(freeswitchId)) {
            return;
        }
        connectionStates.put(freeswitchId, ConnectionState.DEGRADED);
        updateStatsState(freeswitchId, ConnectionState.DEGRADED);
        log.warn("[EslConnection] 连接降级: id={}", freeswitchId);
    }

    /**
     * 断开 ESL 连接。
     */
    public void disconnect(String freeswitchId) {
        if (!isKnownNode(freeswitchId)) {
            return;
        }
        reconnectEnabled.computeIfAbsent(freeswitchId, k -> new AtomicBoolean(true)).set(false);
        connectionClient.disconnect(freeswitchId);
        connectionStates.put(freeswitchId, ConnectionState.DISCONNECTED);
        lastHeartbeatTimes.remove(freeswitchId);
        updateStatsOnDisconnect(freeswitchId);
        log.info("[EslConnection] 已断开 FreeSWITCH 连接: id={}", freeswitchId);
    }

    /**
     * 触发重连。
     * <p>当前实现立即重连，并记录退避时间；后续如接入调度器，可按 backoffMs 延迟执行。</p>
     */
    public void reconnect(String freeswitchId) {
        AtomicBoolean enabled = reconnectEnabled.computeIfAbsent(freeswitchId, k -> new AtomicBoolean(false));
        if (!enabled.get()) {
            log.debug("[EslConnection] 重连已禁用: id={}", freeswitchId);
            return;
        }

        ConnectionConfig cachedConfig = configCache.get(freeswitchId);
        if (cachedConfig == null) {
            log.warn("[EslConnection] 无连接配置缓存，无法重连: id={}", freeswitchId);
            return;
        }

        int attempt = reconnectCounters.computeIfAbsent(freeswitchId, k -> new AtomicInteger(0)).incrementAndGet();
        long backoffMs = Math.min(attempt * 2000L, config.getReconnectMaxBackoffMs());
        connectionStates.put(freeswitchId, ConnectionState.RECONNECTING);
        updateStatsState(freeswitchId, ConnectionState.RECONNECTING);
        log.info("[EslConnection] 第{}次重连尝试: id={}, host={}:{}, 建议退避{}ms",
                attempt, freeswitchId, cachedConfig.host(), cachedConfig.port(), backoffMs);
        connect(freeswitchId, cachedConfig.host(), cachedConfig.port(), cachedConfig.password());
    }

    /**
     * 获取连接状态。
     */
    public ConnectionState getConnectionState(String freeswitchId) {
        return connectionStates.getOrDefault(freeswitchId, ConnectionState.DISCONNECTED);
    }

    /**
     * 获取所有已注册连接状态。
     */
    public Map<String, ConnectionState> getAllConnectionStates() {
        return Map.copyOf(connectionStates);
    }

    /**
     * 更新心跳时间。
     */
    public void updateHeartbeat(String freeswitchId) {
        if (freeswitchId == null || freeswitchId.isBlank()) {
            return;
        }
        LocalDateTime heartbeatTime = LocalDateTime.now();
        lastHeartbeatTimes.put(freeswitchId, heartbeatTime);
        updateStatsHeartbeat(freeswitchId, heartbeatTime);
        if (connectionStates.get(freeswitchId) == ConnectionState.DEGRADED) {
            connectionStates.put(freeswitchId, ConnectionState.CONNECTED);
            updateStatsState(freeswitchId, ConnectionState.CONNECTED);
            log.info("[EslConnection] 心跳恢复: id={}", freeswitchId);
        }
    }

    /**
     * 检查心跳是否正常。
     */
    public boolean isHeartbeatOk(String freeswitchId, long thresholdSec) {
        LocalDateTime lastHb = lastHeartbeatTimes.get(freeswitchId);
        if (lastHb == null) return false;
        return Duration.between(lastHb, LocalDateTime.now()).getSeconds() <= thresholdSec;
    }

    /**
     * 是否已连接。
     */
    public boolean isConnected(String freeswitchId) {
        return connectionStates.getOrDefault(freeswitchId, ConnectionState.DISCONNECTED) == ConnectionState.CONNECTED;
    }

    /**
     * 获取指定节点连接统计。
     */
    public ConnectionStats getConnectionStats(String freeswitchId) {
        return connectionStatsMap.get(freeswitchId);
    }

    private void validateConnectionArgs(String freeswitchId, String host, int port) {
        EslCommandValidator.validateFreeswitchId(freeswitchId);
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("FreeSWITCH host 不能为空");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("FreeSWITCH ESL 端口非法: " + port);
        }
    }

    private boolean isKnownNode(String freeswitchId) {
        return freeswitchId != null && connectionStates.containsKey(freeswitchId);
    }

    private void initStatsIfAbsent(String freeswitchId, String host, int port) {
        connectionStatsMap.putIfAbsent(freeswitchId, new ConnectionStats(
                freeswitchId, host, port, ConnectionState.CONNECTING, 0, 0, null, null, null, 0));
    }

    private void markConnected(String freeswitchId, String host, int port) {
        connectionStates.put(freeswitchId, ConnectionState.CONNECTED);
        lastHeartbeatTimes.put(freeswitchId, LocalDateTime.now());
        reconnectCounters.computeIfAbsent(freeswitchId, k -> new AtomicInteger(0)).set(0);
        reconnectEnabled.computeIfAbsent(freeswitchId, k -> new AtomicBoolean(true)).set(true);
        updateStatsOnConnect(freeswitchId, host, port);
    }

    private void markFailed(String freeswitchId) {
        connectionStates.put(freeswitchId, ConnectionState.FAILED);
        updateStatsState(freeswitchId, ConnectionState.FAILED);
        updateStatsOnFail(freeswitchId);
    }

    private void updateStatsOnConnect(String freeswitchId, String host, int port) {
        connectionStatsMap.compute(freeswitchId, (k, old) -> {
            int connectCount = old == null ? 1 : old.connectCount() + 1;
            int disconnectCount = old == null ? 0 : old.disconnectCount();
            LocalDateTime lastDisconnect = old == null ? null : old.lastDisconnectTime();
            return new ConnectionStats(freeswitchId, host, port,
                    ConnectionState.CONNECTED, connectCount, disconnectCount,
                    LocalDateTime.now(), lastDisconnect, LocalDateTime.now(), 0);
        });
    }

    private void updateStatsOnFail(String freeswitchId) {
        connectionStatsMap.computeIfPresent(freeswitchId, (k, old) ->
                new ConnectionStats(old.freeswitchId(), old.host(), old.port(),
                        ConnectionState.FAILED, old.connectCount(), old.disconnectCount(),
                        old.lastConnectTime(), LocalDateTime.now(), old.lastHeartbeat(), old.uptimeSeconds()));
    }

    private void updateStatsOnDisconnect(String freeswitchId) {
        connectionStatsMap.computeIfPresent(freeswitchId, (k, old) -> {
            long uptime = old.lastConnectTime() != null
                    ? Duration.between(old.lastConnectTime(), LocalDateTime.now()).getSeconds()
                    : 0;
            return new ConnectionStats(old.freeswitchId(), old.host(), old.port(),
                    ConnectionState.DISCONNECTED, old.connectCount(), old.disconnectCount() + 1,
                    old.lastConnectTime(), LocalDateTime.now(), old.lastHeartbeat(), uptime);
        });
    }

    private void updateStatsHeartbeat(String freeswitchId, LocalDateTime heartbeatTime) {
        connectionStatsMap.computeIfPresent(freeswitchId, (k, old) ->
                new ConnectionStats(old.freeswitchId(), old.host(), old.port(), old.state(),
                        old.connectCount(), old.disconnectCount(), old.lastConnectTime(),
                        old.lastDisconnectTime(), heartbeatTime, old.uptimeSeconds()));
    }

    private void updateStatsState(String freeswitchId, ConnectionState state) {
        connectionStatsMap.computeIfPresent(freeswitchId, (k, old) ->
                new ConnectionStats(old.freeswitchId(), old.host(), old.port(), state,
                        old.connectCount(), old.disconnectCount(), old.lastConnectTime(),
                        old.lastDisconnectTime(), old.lastHeartbeat(), old.uptimeSeconds()));
    }
}
