package com.och.security.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 审计事件
 * <p>封装一次审计操作的所有信息，包括操作人、操作类型、目标对象、
 * 请求参数、客户端IP、耗时和链路追踪ID等。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    private String traceId;
    private Long userId;
    private String username;
    private String action;
    private String targetType;
    private String targetId;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private Integer responseStatus;
    private String clientIp;
    private Long durationMs;
    private Instant createdAt;
}
