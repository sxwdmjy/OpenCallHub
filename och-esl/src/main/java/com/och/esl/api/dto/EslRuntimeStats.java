package com.och.esl.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ESL 运行时统计响应 DTO。
 * <p>替代原先的 {@code Map<String, Object>}，提供强类型统计字段。</p>
 *
 * @param activeChannels  当前活跃通道数
 * @param pendingCommands 当前待回执命令数
 * @param dtmfBuffers     当前 DTMF 缓冲区数
 * @author danmo
 */
@Schema(description = "ESL 运行时统计")
public record EslRuntimeStats(
        @Schema(description = "当前活跃通道数") int activeChannels,
        @Schema(description = "当前待回执命令数") int pendingCommands,
        @Schema(description = "当前 DTMF 缓冲区数") int dtmfBuffers
) {}
