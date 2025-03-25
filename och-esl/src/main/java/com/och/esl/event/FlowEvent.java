package com.och.esl.event;

import com.och.common.constant.FlowDataContext;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
@Getter
public class FlowEvent extends ApplicationEvent {


    // 事件类型 1-开始 2-流转 3-结束 4-业务处理
    private final Integer type;

    // 事件
    private final String event;

    // 数据
    private final FlowDataContext data;


    public FlowEvent(Integer type, FlowDataContext data) {
        super(data.getFlowId());
        this.type = type;
        this.event = null;
        this.data = data;
    }

    public FlowEvent(Integer type, String event, FlowDataContext data) {
        super(data.getFlowId());
        this.type = type;
        this.event = event;
        this.data = data;
    }



}
