package com.och.calltask.service;

import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.vo.CallTaskVo;

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
     * @param callTask   呼叫任务
     * @return 呼叫结果
     */
    void executePredictiveCall(CallTaskAssignment assignment, CallTaskVo callTask);

    /**
     * 批量执行预测式外呼
     *
     * @param assignments 任务分配列表
     * @return 呼叫结果列表
     */
    void executeBatchPredictiveCalls(java.util.List<CallTaskAssignment> assignments, CallTaskVo callTask);


}
