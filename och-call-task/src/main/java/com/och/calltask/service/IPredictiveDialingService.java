package com.och.calltask.service;

import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.vo.PredictiveDialingCallResult;

/**
 * 预测式外呼服务接口
 * 
 * @author danmo
 * @date 2025/01/15
 */
public interface IPredictiveDialingService {

    /**
     * 执行预测式外呼
     * 
     * @param assignment 任务分配
     * @return 呼叫结果
     */
    PredictiveDialingCallResult executePredictiveCall(CallTaskAssignment assignment);

    /**
     * 批量执行预测式外呼
     * 
     * @param assignments 任务分配列表
     * @return 呼叫结果列表
     */
    java.util.List<PredictiveDialingCallResult> executeBatchPredictiveCalls(java.util.List<CallTaskAssignment> assignments);

    /**
     * 取消外呼
     * 
     * @param callId 呼叫ID
     * @return 是否成功
     */
    boolean cancelCall(Long callId);

    /**
     * 获取呼叫状态
     * 
     * @param callId 呼叫ID
     * @return 呼叫状态
     */
    String getCallStatus(Long callId);

    /**
     * 获取呼叫结果
     * 
     * @param callId 呼叫ID
     * @return 呼叫结果
     */
    PredictiveDialingCallResult getCallResult(Long callId);
}
