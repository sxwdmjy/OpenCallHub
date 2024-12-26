package com.och.ivr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class FlowNodeVo {

    @Schema(description = "节点唯一标识符")
    private Long id;
    /**
     * 节点名称
     */
    @Schema(description = "节点名称")
    private String name;


    /**
     * 节点类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工  6-转接  7-应答 8-挂机 9-路由 10-子IVR 11-满意度
     */
    @Schema(description = "节点类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工  6-转接  7-应答 8-挂机 9-路由 10-子IVR 11-满意度")
    private Integer type;


    /**
     * 流程ID，用于区分不同流程中的节点
     */
    @Schema(description = "流程ID，用于区分不同流程中的节点")
    private Long flowId;


    /**
     * 节点的属性（例如条件表达式、并行处理等）
     */
    @Schema(description = "节点的属性（例如条件表达式、并行处理等）")
    private String properties;


    /**
     * 节点优先级，数值越小优先级越高
     */
    @Schema(description = "节点优先级，数值越小优先级越高")
    private Integer priority;
}
