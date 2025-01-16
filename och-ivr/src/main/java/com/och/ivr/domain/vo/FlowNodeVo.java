package com.och.ivr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class FlowNodeVo {

    @Schema(description = "节点唯一标识符")
    private String id;
    /**
     * 节点名称
     */
    @Schema(description = "节点名称")
    private String name;


    @Schema(description = "节点类型")
    private String type;

    /**
     * 节点业务类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工  6-转接  7-应答 8-挂机 9-路由 10-子IVR 11-满意度
     */
    @Schema(description = "节点业务类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工  6-转接  7-应答 8-挂机 9-路由 10-子IVR 11-满意度")
    private Integer businessType;

    /**
     * 节点的属性
     */
    @Schema(description = "节点的属性")
    private String properties;

}
