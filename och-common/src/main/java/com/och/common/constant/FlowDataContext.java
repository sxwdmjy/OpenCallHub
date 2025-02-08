package com.och.common.constant;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class FlowDataContext {

    /**
     * 呼叫地址
     */
    private String address;
    /**
     * 腿信息
     */
    private String uniqueId;
    /**
     * 呼叫ID
     */
    private Long callId;
    /**
     * 流程实例ID
     */
    private Long instanceId;
    /**
     * 流程信息
     */
    private Long flowId;

    /**
     * 当前节点ID
     */
    private String currentNodeId;


    /**
     * 当前节点执行记录ID
     */
    private Long currentHistoryId;

    /**
     * 挂机原因
     */
    private String hangUpCause;
}
