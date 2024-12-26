package com.och.common.enums;

import lombok.Getter;

@Getter
public enum FlowNodeTypeEnum {

    //0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工  6-转接  7-应答 8-挂机 9-路由 10-子IVR 11-满意度
    START(0, "FlowStartHandler"),
    END(1, "FlowEndHandler"),
    PLAY(2, "FlowPlaybackHandler"),
    MENU(3, "FlowMenuHandler"),
    RECEIVE(4, "FlowReceiveHandler"),
    HUMAN(5, "FlowHumanHandler"),
    TRANSFER(6, "FlowTransferHandler"),
    ANSWER(7, "FlowAnswerHandler"),
    HANGUP(8, "FlowHangupHandler"),
    ROUTE(9, "FlowRouteHandler"),
    SUB_IVR(10, "FlowSubIvrHandler"),
    SATISFACTION(11, "FlowSatisfactionHandler");

    private final Integer type;

    private final String handler;

    FlowNodeTypeEnum(Integer type, String handler) {
        this.type = type;
        this.handler = handler;
    }

    public static String getHandler(Integer type) {
        for (FlowNodeTypeEnum value : FlowNodeTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getHandler();
            }
        }
        return null;
    }
}
