package com.och.common.enums;

import lombok.Getter;

@Getter
public enum SipAgentStatusEnum {

    //状态 0-空闲  1-忙碌 2-通话中 3-离线
    READY(1,"空闲"),
    NOT_READY(2,"忙碌"),
    NOT_READY_NOT_TALKING(3,"勿扰"),
    OFF_ON(4,"离线"),
    TALKING_IN(5,"通话中"),
    RINGING(6,"振铃中"),
    TALKING_OUT(7,"话后");

    ;

    //状态码
    private int code;

    //状态描述
    private String des;

    SipAgentStatusEnum(int code, String des) {
        this.code = code;
        this.des = des;
    }

    public static boolean isEquals(int code, String des) {
        return SipAgentStatusEnum.valueOf(des).code == code;
    }
}
