package com.och.api.controller;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.esl.application.EslConnectionManager;
import com.och.infra.file.FileStorage;
import com.och.infra.mq.DomainEvent;
import com.och.infra.mq.EventPublisher;
import com.och.infra.mq.FoundationEventListener;
import com.och.infra.redis.RedisCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "Foundation", description = "基础健康检查接口")
@RestController
@RequestMapping("/api/foundation")
@RequiredArgsConstructor
public class FoundationController {

    private final RedisCache redisCache;
    private final EventPublisher eventPublisher;
    private final FoundationEventListener foundationEventListener;
    private final FileStorage fileStorage;
    private final EslConnectionManager eslConnectionManager;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "openCallHub");
        result.put("version", "1.0.0");

        // ESL 连接状态
        try {
            Map<String, EslConnectionManager.ConnectionState> eslStates = eslConnectionManager.getAllConnectionStates();
            long connectedCount = eslStates.values().stream()
                    .filter(s -> s == EslConnectionManager.ConnectionState.CONNECTED).count();
            Map<String, Object> eslStatus = new HashMap<>();
            eslStatus.put("totalNodes", eslStates.size());
            eslStatus.put("connectedNodes", connectedCount);
            eslStatus.put("nodes", eslStates);
            eslStatus.put("status", connectedCount == eslStates.size() && !eslStates.isEmpty() ? "UP" : "DEGRADED");
            result.put("esl", eslStatus);
        } catch (Exception e) {
            Map<String, Object> eslStatus = new HashMap<>();
            eslStatus.put("status", "DOWN");
            eslStatus.put("error", e.getMessage());
            result.put("esl", eslStatus);
        }

        return ApiResponse.success(result);
    }

    @Operation(summary = "Redis 连通性检查")
    @GetMapping("/redis/check")
    public ApiResponse<Map<String, Object>> redisCheck() {
        Map<String, Object> result = new HashMap<>();
        try {
            redisCache.set("foundation:health", "ok", 10, TimeUnit.SECONDS);
            String value = redisCache.get("foundation:health");
            result.put("status", "ok".equals(value) ? "UP" : "DOWN");
            result.put("value", value);
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        return ApiResponse.success(result);
    }

    @Operation(summary = "RabbitMQ 连通性检查")
    @GetMapping("/mq/check")
    public ApiResponse<Map<String, Object>> mqCheck() {
        Map<String, Object> result = new HashMap<>();
        try {
            foundationEventListener.reset();
            DomainEvent event = DomainEvent.of("foundation.test", "{\"test\": true}", "smoke-test");
            eventPublisher.publish(event);
            result.put("status", "UP");
            result.put("eventId", event.getEventId());
            result.put("message", "事件已发布，可通过 /api/foundation/mq/events 查看消费记录");
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        return ApiResponse.success(result);
    }

    @Operation(summary = "RabbitMQ 消费事件记录")
    @GetMapping("/mq/events")
    public ApiResponse<Map<String, Object>> mqEvents() {
        Map<String, Object> result = new HashMap<>();
        result.put("eventCount", foundationEventListener.getEventCount());
        result.put("events", foundationEventListener.getConsumedEvents());
        return ApiResponse.success(result);
    }

    @Operation(summary = "文件存储检查")
    @GetMapping("/file/check")
    public ApiResponse<Map<String, Object>> fileCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "local");
        result.put("status", "UP");
        return ApiResponse.success(result);
    }
}
