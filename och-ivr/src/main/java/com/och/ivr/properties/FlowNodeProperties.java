package com.och.ivr.properties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FlowNodeProperties {

    /**
     * 节点业务类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工  6-转接  7-应答 8-挂机 9-路由 10-子IVR 11-满意度
     */
    @Schema(description = "节点业务类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工  6-转接  7-应答 8-挂机 9-路由 10-子IVR 11-满意度")
    public Integer businessType;
}
