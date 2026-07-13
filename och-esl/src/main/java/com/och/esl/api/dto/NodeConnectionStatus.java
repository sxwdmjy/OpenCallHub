package com.och.esl.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 单个 FreeSWITCH 节点连接状态响应 DTO。
 *
 * @param freeswitchId FreeSWITCH 节点标识
 * @param state        连接状态枚举名称
 * @param connected    是否已连接
 * @author danmo
 */
@Schema(description = "单个节点连接状态")
public record NodeConnectionStatus(
        @Schema(description = "FreeSWITCH 节点标识") String freeswitchId,
        @Schema(description = "连接状态") String state,
        @Schema(description = "是否已连接") boolean connected
) {}
