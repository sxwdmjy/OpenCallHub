package com.och.esl.application;

import com.och.esl.config.EslConfigurationProperties;
import com.och.esl.application.DtmfProcessingService;
import lombok.extern.slf4j.Slf4j;
import com.och.esl.application.DtmfProcessingService;
import lombok.RequiredArgsConstructor;
import com.och.esl.application.DtmfProcessingService;
import org.springframework.scheduling.annotation.Scheduled;
import com.och.esl.application.DtmfProcessingService;
import org.springframework.stereotype.Component;

/**
 * ESL 定时清理任务
 * <p>定期执行以下维护操作，保持 ESL 运行时状态的健康：</p>
 * <ul>
 *   <li><b>心跳看门狗</b>：检查各 FreeSWITCH 节点心跳是否超时，超时则触发 DEGRADED → RECONNECTING</li>
 *   <li><b>DTMF 缓冲区清理</b>：清理超时未完成输入的按键缓冲区</li>
 *   <li><b>命令回执清理</b>：清理超时未收到响应的待回执命令</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledCleanupTask {

    private final EslConnectionManager connectionManager;
    private final DtmfProcessingService dtmfService;
    private final EslCommandResultPublisher commandPublisher;
    private final EslConfigurationProperties config;

    /**
     * 心跳看门狗：每 10 秒检查一次
     * <p>遍历所有已连接的节点，检查心跳是否超时（heartbeatMissThreshold × heartbeatIntervalMs）。
     * 超时则将状态标记为 DEGRADED，
     * 持续超时超过 2 倍阈值则触发 RECONNECTING。</p>
     */
    @Scheduled(fixedDelay = 10_000)
    public void heartbeatWatchdog() {
        long thresholdMs = (long) config.getHeartbeatMissThreshold() * config.getHeartbeatIntervalMs();
        long thresholdSec = thresholdMs / 1000;

        connectionManager.getAllConnectionStates().forEach((fsId, state) -> {
            if (state != EslConnectionManager.ConnectionState.CONNECTED
                    && state != EslConnectionManager.ConnectionState.DEGRADED) {
                return; // 只检查 CONNECTED 和 DEGRADED 状态
            }

            if (!connectionManager.isHeartbeatOk(fsId, thresholdSec)) {
                if (state == EslConnectionManager.ConnectionState.DEGRADED) {
                    // 持续超时 → 触发重连
                    log.warn("[HeartbeatWatchdog] 心跳持续超时，触发重连: id={}", fsId);
                    connectionManager.reconnect(fsId);
                } else {
                    // 首次超时 → 降级
                    log.warn("[HeartbeatWatchdog] 心跳超时，状态降级: id={}, threshold={}s", fsId, thresholdSec);
                    connectionManager.markDegraded(fsId);
                }
            }
        });
    }

    /**
     * DTMF 缓冲区清理：每 30 秒清理一次过期缓冲区
     */
    @Scheduled(fixedDelay = 30_000)
    public void cleanDtmfBuffers() {
        int removed = dtmfService.cleanExpired();
        if (removed > 0) {
            log.info("[ScheduledCleanup] 清理过期DTMF缓冲区: {} 个", removed);
        }
    }

    /**
     * 命令回执清理：每 60 秒清理一次超时命令
     */
    @Scheduled(fixedDelay = 60_000)
    public void cleanExpiredCommands() {
        int removed = commandPublisher.cleanExpired(120);
        if (removed > 0) {
            log.info("[ScheduledCleanup] 清理超时命令回执: {} 个", removed);
        }
    }
}
