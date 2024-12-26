package com.och.esl.event;

import com.och.common.constant.FlowDataContext;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
@Getter
public class FlowEvent extends ApplicationEvent {

    // 流程ID
    private final Long flowId;

    // 事件类型 1-开始 2-流转 3-结束
    private final Integer type;

    // 事件
    private final String event;

    // 数据
    private final FlowDataContext data;

    // 流程实例ID
    public Long instanceId;

    public FlowEvent(Long flowId, Integer type, Long instanceId, FlowDataContext data) {
        super(flowId);
        this.flowId = flowId;
        this.type = type;
        this.event = null;
        this.instanceId = instanceId;
        this.data = data;
    }

    public FlowEvent(Long flowId, Integer type, String event, Long instanceId, FlowDataContext data) {
        super(flowId);
        this.flowId = flowId;
        this.type = type;
        this.event = event;
        this.instanceId = instanceId;
        this.data = data;
    }



}
