package com.och.esl.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * ESL 连接状态汇总响应 DTO。
 * <p>替代原先的 {@code Map<String, Object>}，提供强类型字段。</p>
 *
 * @param connections    各节点连接状态（节点标识 → 状态名称）
 * @param totalNodes     总节点数
 * @param connectedNodes 已连接节点数
 * @author danmo
 */
@Schema(description = "ESL 连接状态汇总")
public record EslConnectionStatus(
        @Schema(description = "各节点连接状态，key=节点标识, value=状态名称") Map<String, String> connections,
        @Schema(description = "总节点数") int totalNodes,
        @Schema(description = "已连接节点数") long connectedNodes
) {}
