package com.och.esl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ESL 连接配置属性。
 * <p>从 application.yml 的 {@code och.esl} 前缀读取全局 ESL 运行参数。
 * FreeSWITCH 各节点的连接信息（host、port、password）由 {@code fs_node} 表动态读取，
 * 在 {@link com.och.esl.application.EslConnectionManager#connect connect()} 时传入，
 * 此处仅保留连接超时、心跳间隔、重连退避等非节点级别的全局配置。</p>
 *
 * <h3>配置示例</h3>
 * <pre>
 * och:
 *   esl:
 *     connect-timeout-ms: 10000
 *     heartbeat-interval-ms: 20000
 *     heartbeat-miss-threshold: 3
 *     reconnect-max-backoff-ms: 30000
 *     event-buffer-size: 2000
 *     tls-enabled: false
 * </pre>
 *
 * @author danmo
 */
@Data
@Component
@ConfigurationProperties(prefix = "och.esl")
public class EslConfigurationProperties {

    /** ESL 连接超时时间（毫秒），默认 10000 */
    private int connectTimeoutMs = 10000;

    /** ESL 心跳间隔（毫秒），默认 20000 */
    private int heartbeatIntervalMs = 20000;

    /** 心跳丢失阈值（连续丢失 N 次视为断连），默认 3 */
    private int heartbeatMissThreshold = 3;

    /** 重连最大退避时间（毫秒），默认 30000 */
    private int reconnectMaxBackoffMs = 30000;

    /** 事件缓冲区大小，默认 2000 */
    private int eventBufferSize = 2000;

    /** SSL/TLS 是否启用，默认 false */
    private boolean tlsEnabled = false;
}
