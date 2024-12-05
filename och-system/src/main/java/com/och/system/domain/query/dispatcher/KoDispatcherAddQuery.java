package com.och.system.domain.query.dispatcher;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月11日 15:01
 */
@Schema
@Data
public class KoDispatcherAddQuery {

    @Schema(description = "ID",hidden = true)
    private Integer id;

    @NotNull(message = "分组ID不能为空")
    @Schema(description = "分组ID",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer groupId;

    @NotBlank(message = "目标URL不能为空")
    @Schema(description = "目标URL",requiredMode = Schema.RequiredMode.REQUIRED)
    private String destination;

    @Schema(description = "标识")
    private Integer flags;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "属性")
    private String attrs;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态 0-在线 1-下线")
    private Integer status;
}
