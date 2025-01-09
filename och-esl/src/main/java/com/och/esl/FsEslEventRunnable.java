package com.och.esl;

import com.och.esl.factory.FsEslEventFactory;
import com.och.esl.utils.EslEventUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author danmo
 * @date 2023-10-20 17:19
 **/
@Slf4j
public class FsEslEventRunnable implements Runnable {

    private final FsEslEventFactory factory;

    @Getter
    private final FsEslMsg msg;

    public FsEslEventRunnable(FsEslEventFactory factory, FsEslMsg msg) {
        this.factory = factory;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            log.debug("【接收EslEvent事件消息消费】 {}, {}", msg.getEslEvent().getEventName(), EslEventUtil.getUniqueId(msg.getEslEvent()));
            factory.getResource(msg.getAddress(), msg.getEslEvent());
        } catch (Exception e) {
            log.error("EslEvent事件消息消费失败 {}, {}", msg.getEslEvent().getEventName(), EslEventUtil.getUniqueId(msg.getEslEvent()),e);
        }
    }
}
