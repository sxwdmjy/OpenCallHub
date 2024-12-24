package com.och.ivr.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@Slf4j
public class FlowStateMachineListener extends StateMachineListenerAdapter<Object, Object> {

    @Override
    public void stateEntered(State<Object, Object> state) {
        log.info("状态机状态进入:{}", state.getId());
    }

    @Override
    public void stateExited(State<Object, Object> state) {
        log.info("状态机状态退出:{}", state.getId());
    }

    @Override
    public void stateMachineError(StateMachine<Object, Object> stateMachine, Exception exception) {
        log.info("状态机异常 id:{}, error:{}", stateMachine.getId(), exception.getMessage());
    }
}
