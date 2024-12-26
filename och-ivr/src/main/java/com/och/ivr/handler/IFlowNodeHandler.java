package com.och.ivr.handler;

import org.springframework.statemachine.StateContext;

public interface IFlowNodeHandler {

    void handle(StateContext<Object, Object> stateContext);
}
