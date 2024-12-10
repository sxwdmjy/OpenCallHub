package com.och.common.enums;

import lombok.Getter;

@Getter
public enum HangupCauseEnum {

    /**
     * 正常挂机
     */
    NORMAL_CLEARING(1, "正常挂机"),

    AGENT_NO_BAND_SIP(2, "坐席未绑定SIP号码"),
    NOT_ROUTE(3, "未配置号码路由"),
    ROUTE_NOT_GATEWAY(4, "路由未配置网关"),
    /**
     * 坐席全忙挂机
     */
    FULLBUSY(10, "坐席全忙挂机"),

    /**
     * 排队超时挂机
     */
    QUEUE_TIME_OUT(11, "排队超时挂机"),
    /**
     * 溢出挂机
     */
    OVERFLOW(12, "溢出挂机"),

    SKILL_NO_AGENT(21, "技能组未配置坐席"),
    ;

    private Integer code;


    private String desc;

    HangupCauseEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
