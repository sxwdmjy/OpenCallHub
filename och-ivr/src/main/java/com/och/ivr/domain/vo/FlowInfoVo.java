package com.och.ivr.domain.vo;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class FlowInfoVo extends BaseVo {

    @Schema(description = "流程实例唯一标识符")
    private Long id;

    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "ivr名称")
    private String name;

    @Schema(description = "流程描述")
    private String desc;

    @Schema(description = "流程状态 0-草稿 1-待发布 2-已发布")
    private Integer status;

    @Schema(description = "流程数据")
    private String flowData;

}
