package com.och.ivr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class FlowEdgeVo {

    /**
     * 边的唯一标识符
     */

    @Schema(description = "边的唯一标识符")
    private String id;

    /**
     * 源节点ID
     */
    @Schema(description = "源节点ID")
    private String sourceNodeId;

    /**
     * 目标节点ID
     */
    @Schema(description = "目标节点ID")
    private String targetNodeId;


    @Schema(description = "事件")
    private String event;
}
