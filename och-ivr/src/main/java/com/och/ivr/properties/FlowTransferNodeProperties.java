package com.och.ivr.properties;

import lombok.Data;

@Data
public class FlowTransferNodeProperties implements FlowNodeProperties{

    /**
     * 路由类型 1-坐席 2-外呼 3-sip 4-技能组
     */
    private Integer routeType;

    /**
     * 路由类型值ID
     */
    private Long routeValueId;
    /**
     * 路由类型值
     */
    private String routeValue;
}
