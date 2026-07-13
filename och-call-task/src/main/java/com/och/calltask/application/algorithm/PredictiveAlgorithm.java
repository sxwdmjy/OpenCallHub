package com.och.calltask.application.algorithm;

import com.och.calltask.domain.OutboundDialAttempt;

/**
 * 预测算法引擎接口（策略模式）
 * <p>
 * 提供号码接通概率预测、最优拨号数计算、模型更新三个核心能力。
 * 不同算法实现可通过 Spring 依赖注入进行切换。
 * </p>
 *
 * @author danmo
 */
public interface PredictiveAlgorithm {

    /**
     * 预测指定号码的接通概率
     *
     * @param phoneNumber 目标电话号码
     * @return 接通概率（0.0 ~ 1.0）
     */
    double predictAnswerProbability(String phoneNumber);

    /**
     * 根据空闲坐席数和接通率，计算本次应发起的最优拨号数
     *
     * @param idleAgents 当前空闲坐席数
     * @param answerRate 当前预估接通率（0.0 ~ 1.0）
     * @return 建议拨号数量
     */
    int calculateOptimalDialCount(int idleAgents, double answerRate);

    /**
     * 根据拨号结果更新预测模型
     *
     * @param attempt 拨号尝试记录（包含拨号结果）
     */
    void updateModel(OutboundDialAttempt attempt);
}
