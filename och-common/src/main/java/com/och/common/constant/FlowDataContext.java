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

    /**
     * 语音识别引擎
     */
    private String asrEngine;
    /**
     * 语音合成引擎
     */
    private String ttsEngine;

    /**
     * 语音合成音色
     */
    private String ttsVoice;

    /**
     * http请求结果
     */
    private String httpResult;
}
