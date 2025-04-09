package com.och.ivr.handler.node;

import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import org.springframework.statemachine.StateContext;

public interface IFlowNodeHandler {

    void handle(StateContext<Object, Object> stateContext);

    default void businessHandler(String event, FlowDataContext flowData) throws FlowNodeException{
        throw new FlowNodeException("businessHandler is not implemented");
    }
}
