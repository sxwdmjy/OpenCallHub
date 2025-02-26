package com.och.ivr.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowTransferNodeProperties extends FlowNodeProperties{

    /**
     * 路由类型 1-坐席 2-外呼 3-sip 4-技能组
     *
     */
    private Integer routeType;

    /**
     * 路由类型值
     */
    private String routeValue;
}
