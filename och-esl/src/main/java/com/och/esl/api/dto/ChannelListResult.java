package com.och.esl.api.dto;

import com.och.esl.application.ChannelStateTracker;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 活跃通道列表响应 DTO。
 * <p>替代原先的 {@code Map<String, Object>}，提供强类型字段，
 * 便于 Swagger 文档生成和调用方代码绑定。</p>
 *
 * @param total    活跃通道总数
 * @param filtered 过滤后的通道数（当传入 filter 参数时有效）
 * @param page     当前页码（从 0 开始）
 * @param size     每页大小
 * @param hasMore  是否有更多数据
 * @param channels 当前页的通道快照列表
 * @author danmo
 */
@Schema(description = "活跃通道列表响应")
public record ChannelListResult(
        @Schema(description = "活跃通道总数") int total,
        @Schema(description = "过滤后的通道数") int filtered,
        @Schema(description = "当前页码（从 0 开始）") int page,
        @Schema(description = "每页大小") int size,
        @Schema(description = "是否有更多数据") boolean hasMore,
        @Schema(description = "当前页通道快照列表") List<ChannelStateTracker.ChannelSnapshot> channels
) {}
