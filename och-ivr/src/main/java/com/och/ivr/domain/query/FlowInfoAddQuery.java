package com.och.ivr.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema
@Data
public class FlowInfoAddQuery {

    @Schema(description = "流程唯一标识符", hidden = true)
    private Long id;

    @NotNull(message = "分组ID不能为空")
    @Schema(description = "分组ID")
    private Long groupId;

    @NotBlank(message = "名称不能为空")
    @Schema(description = "ivr名称")
    private String name;

    @NotBlank(message = "描述不能为空")
    @Schema(description = "IVR描述")
    private String desc;

    @NotNull(message = "状态不能为空")
    @Schema(description = "流程状态 0-草稿 1-待发布 2-已发布")
    private Integer status;

    @Schema(description = "流程数据")
    private String flowData;
}
