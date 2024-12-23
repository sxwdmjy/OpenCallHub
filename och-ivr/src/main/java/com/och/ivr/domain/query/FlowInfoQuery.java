package com.och.ivr.domain.query;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema
@Data
public class FlowInfoQuery extends BaseQuery {

    @Schema(description = "流程实例唯一标识符")
    private Long id;

    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "ivr名称")
    private String name;

    @Schema(description = "流程描述")
    private String desc;

    @Schema(description = "流程状态 0-草稿 1-待发布 2-已发布")
    private Integer status;

}
