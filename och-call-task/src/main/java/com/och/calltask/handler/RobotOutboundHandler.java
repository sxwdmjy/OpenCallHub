package com.och.calltask.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 机器人外呼
 * @author: danmo
 * @date: 2025/10/07
 */

@RequiredArgsConstructor
@Slf4j
@Component
public class RobotOutboundHandler implements CallTaskHandler{


    @Override
    public void execute(Long taskId) {

    }
}
