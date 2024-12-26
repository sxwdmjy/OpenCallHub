package com.och.ivr.contants;

import com.och.ivr.domain.vo.FlowInfoVo;
import lombok.Data;

@Data
public class FlowData {

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
    private FlowInfoVo flowInfoVo;

    /**
     * 当前节点执行记录ID
     */
    private Long currentHistoryId;
}
