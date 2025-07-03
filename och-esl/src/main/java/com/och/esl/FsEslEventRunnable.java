package com.och.esl;

import cn.hutool.core.util.IdUtil;
import com.och.common.utils.StringUtils;
import com.och.common.utils.TraceUtil;
import com.och.esl.factory.FsEslEventFactory;
import com.och.esl.utils.EslEventUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;

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
            TraceUtil.setTraceId(StringUtils.isEmpty(EslEventUtil.getUniqueId(msg.getEslEvent()))? IdUtil.fastSimpleUUID() : EslEventUtil.getUniqueId(msg.getEslEvent()));
            log.debug("【接收EslEvent事件消息消费】 {}, {}", msg.getEslEvent().getEventName(), EslEventUtil.getUniqueId(msg.getEslEvent()));
            factory.getResource(msg.getAddress(), msg.getEslEvent());
        } catch (Exception e) {
            log.error("EslEvent事件消息消费失败 {}, {}", msg.getEslEvent().getEventName(), EslEventUtil.getUniqueId(msg.getEslEvent()),e);
        }finally {
            TraceUtil.clear();
        }
    }
}
