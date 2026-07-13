package com.och.api.service;

import com.och.call.domain.CallSession;

/**
 * 呼叫流程编排服务接口
 * <p>负责呼入全链路编排：Call → IVR → ACD，作为各模块的聚合入口。</p>
 *
 * @author danmo
 */
public interface ICallFlowService {

    /**
     * 启动呼入流程
     *
     * @param callId        呼叫唯一标识
     * @param calledNumber  被叫号码
     * @param callerNumber  主叫号码
     * @return 更新后的呼叫会话
     */
    CallSession startInboundFlow(String callId, String calledNumber, String callerNumber);

    /**
     * DTMF按键事件处理
     *
     * @param callId  呼叫ID
     * @param digit   按键值
     */
    void handleDtmf(String callId, String digit);

    /**
     * 查询当前呼叫流程状态
     *
     * @param callId 呼叫ID
     * @return 流程状态摘要
     */
    FlowStatus getFlowStatus(String callId);

    record FlowStatus(String callId, String callState, String ivrNodeName, String queueState, Long agentId) {}
}