package com.och.calltask.service;

import com.och.calltask.domain.vo.PredictiveDialingMetrics;
import com.och.calltask.domain.vo.PredictiveDialingResult;

import java.util.List;

/**
 * 预测式外呼算法服务接口
 * 
 * @author danmo
 * @date 2025/01/15
 */
public interface IPredictiveAlgorithmService {

    /**
     * 计算预测拨号数量
     * 
     * @param taskId 任务ID
     * @param availableAgents 可用坐席数量
     * @param historicalMetrics 历史指标
     * @return 预测拨号结果
     */
    PredictiveDialingResult calculateOptimalDialCount(Long taskId, int availableAgents, PredictiveDialingMetrics historicalMetrics);

    /**
     * 预测客户接通概率
     * 
     * @param contactId 客户ID
     * @param taskId 任务ID
     * @return 接通概率 (0-1)
     */
    double predictContactAnswerProbability(Long contactId, Long taskId);

    /**
     * 计算最优拨号时间
     * 
     * @param contactId 客户ID
     * @param taskId 任务ID
     * @return 建议拨号时间（分钟）
     */
    int calculateOptimalDialTime(Long contactId, Long taskId);

    /**
     * 更新预测模型
     * 
     * @param taskId 任务ID
     * @param actualResults 实际结果
     */
    void updatePredictionModel(Long taskId, PredictiveDialingMetrics actualResults);

    /**
     * 获取任务预测指标
     * 
     * @param taskId 任务ID
     * @return 预测指标
     */
    PredictiveDialingMetrics getTaskPredictionMetrics(Long taskId);

    /**
     * 批量预测客户价值
     * 
     * @param contactIds 客户ID列表
     * @param taskId 任务ID
     * @return 客户价值评分列表
     */
    List<Double> batchPredictContactValue(List<Long> contactIds, Long taskId);
}
