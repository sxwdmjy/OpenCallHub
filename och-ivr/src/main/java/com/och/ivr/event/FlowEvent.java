package com.och.ivr.event;

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

    // 节点ID
    private final Long nodeId;

    private final String data;

    // 流程实例ID
    public Long instanceId;

    public FlowEvent(Long flowId, Integer type, Long instanceId) {
        super(flowId);
        this.flowId = flowId;
        this.type = type;
        this.instanceId = instanceId;
        this.nodeId = null;
        this.data = null;
    }

    public FlowEvent(Long flowId, Integer type, Long nodeId,  String data) {
        super(flowId);
        this.flowId = flowId;
        this.type = type;
        this.nodeId = nodeId;
        this.data = data;
    }
}
