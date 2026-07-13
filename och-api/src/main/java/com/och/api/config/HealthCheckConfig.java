package com.och.api.config;

import com.och.esl.application.EslConnectionManager;
import com.och.esl.application.EslConnectionManager.ConnectionState;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 扩展健康检查端点
 * <p>在 /actuator/health 基础上增加 FreeSWITCH ESL 连接状态、文件存储状态检查。</p>
 */
@Configuration
@RequiredArgsConstructor
public class HealthCheckConfig {

    private final EslConnectionManager eslConnectionManager;

    /** FreeSWITCH ESL 连接健康检查 */
    @Bean
    public HealthIndicator freeswitchHealth() {
        return () -> {
            Map<String, ConnectionState> states = eslConnectionManager.getAllConnectionStates();
            if (states.isEmpty()) {
                return Health.unknown()
                        .withDetail("freeswitch", "no_nodes_registered")
                        .build();
            }
            long connectedCount = states.values().stream()
                    .filter(s -> s == ConnectionState.CONNECTED)
                    .count();
            Map<String, String> nodeDetails = new HashMap<>();
            states.forEach((id, state) -> nodeDetails.put(id, state.name()));

            if (connectedCount == states.size()) {
                return Health.up()
                        .withDetail("freeswitch", "connected")
                        .withDetail("total_nodes", states.size())
                        .withDetail("connected_nodes", connectedCount)
                        .withDetail("nodes", nodeDetails)
                        .build();
            }
            return Health.down()
                    .withDetail("freeswitch", "partial_or_disconnected")
                    .withDetail("total_nodes", states.size())
                    .withDetail("connected_nodes", connectedCount)
                    .withDetail("nodes", nodeDetails)
                    .build();
        };
    }

    /** 文件存储健康检查 */
    @Bean
    public HealthIndicator fileStorageHealth() {
        return () -> Health.up()
                .withDetail("storage_type", "local")
                .withDetail("base_path", "./data/files")
                .build();
    }
}
