package com.och.ivr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class FlowNodeVo {

    @Schema(description = "节点唯一标识符")
    private String id;



    @Schema(description = "节点类型")
    private String type;

    /**
     * 节点的属性
     */
    @Schema(description = "节点的属性")
    private String properties;

}
