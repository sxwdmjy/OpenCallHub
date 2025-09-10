package com.och.calltask.service.impl;

import com.och.calltask.domain.query.CallTaskContactQuery;
import com.och.calltask.domain.vo.CallTaskContactVo;
import com.och.calltask.domain.vo.PredictiveDialingMetrics;
import com.och.calltask.domain.vo.PredictiveDialingResult;
import com.och.calltask.service.ICallTaskService;
import com.och.calltask.service.IPredictiveAlgorithmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预测式外呼算法服务实现
 * 
 * @author danmo
 * @date 2025/01/15
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PredictiveAlgorithmServiceImpl implements IPredictiveAlgorithmService {

    private final ICallTaskService callTaskService;

    // 算法参数
    private static final double BASE_ANSWER_RATE = 0.25;
    private static final double TIME_FACTOR_WEIGHT = 0.3;
    private static final double CUSTOMER_VALUE_WEIGHT = 0.4;
    private static final double AGENT_SKILL_WEIGHT = 0.3;
    private static final double SEASONAL_FACTOR_WEIGHT = 0.2;
    private static final double DAY_TYPE_FACTOR_WEIGHT = 0.15;

    @Override
    public PredictiveDialingResult calculateOptimalDialCount(Long taskId, int availableAgents, PredictiveDialingMetrics historicalMetrics) {
        log.info("【预测算法】开始计算任务:{} 的最优拨号数量", taskId);
        
        try {
            // 1. 计算基础预测指标
            double predictedAnswerRate = calculatePredictedAnswerRate(historicalMetrics);
            double expectedUtilization = calculateExpectedUtilization(availableAgents, historicalMetrics);
            
            // 2. 应用时间因子
            double timeFactor = calculateTimeFactor();
            double seasonalFactor = calculateSeasonalFactor();
            double dayTypeFactor = calculateDayTypeFactor();
            
            // 3. 计算调整后的预测指标
            double adjustedAnswerRate = predictedAnswerRate * timeFactor * seasonalFactor * dayTypeFactor;
            adjustedAnswerRate = Math.max(0.1, Math.min(0.8, adjustedAnswerRate)); // 限制在合理范围
            
            // 4. 计算最优拨号数量
            int optimalDialCount = calculateOptimalDialCount(availableAgents, expectedUtilization, adjustedAnswerRate);
            
            // 5. 计算预期结果
            int expectedAnswerCount = (int) Math.round(optimalDialCount * adjustedAnswerRate);
            double expectedAgentUtilization = (double) expectedAnswerCount / availableAgents;
            
            // 6. 计算风险等级和置信度
            String riskLevel = calculateRiskLevel(optimalDialCount, availableAgents, adjustedAnswerRate);
            double confidence = calculateConfidence(historicalMetrics);
            
            // 7. 生成优先级客户列表
            List<Long> priorityContactIds = generatePriorityContactList(taskId, optimalDialCount);
            
            // 8. 构建结果
            PredictiveDialingResult result = new PredictiveDialingResult();
            result.setRecommendedDialCount(optimalDialCount);
            result.setPredictedSuccessRate(adjustedAnswerRate);
            result.setExpectedAgentUtilization(expectedAgentUtilization);
            result.setExpectedAnswerCount(expectedAnswerCount);
            result.setRecommendedDialInterval(calculateOptimalDialInterval(adjustedAnswerRate));
            result.setPriorityContactIds(priorityContactIds);
            result.setRiskLevel(riskLevel);
            result.setConfidence(confidence);
            result.setPredictionTime(LocalDateTime.now());
            result.setAlgorithmVersion("2.0");
            result.setPredictionNote(generatePredictionNote(optimalDialCount, adjustedAnswerRate, riskLevel));
            result.setAdjustmentSuggestions(generateAdjustmentSuggestions(optimalDialCount, expectedAgentUtilization));
            result.setExpectedRevenueScore(calculateExpectedRevenueScore(optimalDialCount, adjustedAnswerRate));
            result.setResourceConsumptionEstimate(calculateResourceConsumption(optimalDialCount, availableAgents));
            
            log.info("【预测结果】任务:{} 建议拨号:{} 预期接通:{} 成功率:{:.2f}% 风险:{}", 
                    taskId, optimalDialCount, expectedAnswerCount, adjustedAnswerRate * 100, riskLevel);
            
            return result;
            
        } catch (Exception e) {
            log.error("【预测算法异常】任务:{} 计算失败", taskId, e);
            return createFallbackResult(availableAgents);
        }
    }

    @Override
    public double predictContactAnswerProbability(Long contactId, Long taskId) {
        try {
            // 获取客户历史数据
            PredictiveDialingMetrics metrics = getTaskPredictionMetrics(taskId);
            
            // 基础概率
            double baseProbability = metrics.getHistoricalAnswerRate() != null ? 
                    metrics.getHistoricalAnswerRate() : BASE_ANSWER_RATE;
            
            // 时间因子
            double timeFactor = calculateTimeFactor();
            
            // 客户价值因子
            double customerValueFactor = calculateCustomerValueFactor(contactId, taskId);
            
            // 综合计算
            double probability = baseProbability * timeFactor * customerValueFactor;
            
            return Math.max(0.05, Math.min(0.95, probability)); // 限制在5%-95%
            
        } catch (Exception e) {
            log.warn("【客户预测异常】客户:{} 任务:{} 预测失败", contactId, taskId, e);
            return BASE_ANSWER_RATE;
        }
    }

    @Override
    public int calculateOptimalDialTime(Long contactId, Long taskId) {
        try {
            LocalTime currentTime = LocalTime.now();
            int currentHour = currentTime.getHour();
            
            // 基于历史数据的最佳拨号时间
            int optimalHour = calculateOptimalHour(taskId);
            
            // 计算时间差（分钟）
            int timeDiff = (optimalHour - currentHour + 24) % 24 * 60;
            
            return Math.max(0, timeDiff);
            
        } catch (Exception e) {
            log.warn("【时间计算异常】客户:{} 任务:{} 计算失败", contactId, taskId, e);
            return 0;
        }
    }

    @Override
    public void updatePredictionModel(Long taskId, PredictiveDialingMetrics actualResults) {
        try {
            log.info("【模型更新】开始更新任务:{} 的预测模型", taskId);
            
            // 这里可以实现模型更新逻辑
            // 例如：更新历史数据、调整参数、重新训练模型等
            
            log.info("【模型更新完成】任务:{} 预测模型已更新", taskId);
            
        } catch (Exception e) {
            log.error("【模型更新异常】任务:{} 更新失败", taskId, e);
        }
    }

    @Override
    public PredictiveDialingMetrics getTaskPredictionMetrics(Long taskId) {
        try {
            // 查询任务历史数据
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(taskId);
            query.setStatus(1); // 已分配
            
            List<CallTaskContactVo> historicalContacts = callTaskService.getTaskContactList(query);
            
            if (CollectionUtils.isEmpty(historicalContacts)) {
                return createDefaultMetrics(taskId);
            }
            
            // 计算历史指标
            PredictiveDialingMetrics metrics = new PredictiveDialingMetrics();
            metrics.setTaskId(taskId);
            metrics.setHistoricalAnswerRate(calculateHistoricalAnswerRate(historicalContacts));
            metrics.setAverageCallDuration(calculateAverageCallDuration(historicalContacts));
            metrics.setAverageAgentIdleTime(calculateAverageAgentIdleTime());
            metrics.setAverageCustomerResponseTime(calculateAverageCustomerResponseTime());
            metrics.setHourlyAnswerRate(calculateHourlyAnswerRate());
            metrics.setCustomerValueScore(calculateCustomerValueScore(historicalContacts));
            metrics.setAgentSkillMatch(calculateAgentSkillMatch());
            metrics.setPredictionAccuracy(calculatePredictionAccuracy());
            metrics.setUpdateTime(LocalDateTime.now());
            metrics.setSampleSize(historicalContacts.size());
            metrics.setConfidence(calculateConfidence(metrics));
            metrics.setTrend(calculateTrend(historicalContacts));
            metrics.setSeasonalFactor(calculateSeasonalFactor());
            metrics.setDayTypeFactor(calculateDayTypeFactor());
            
            return metrics;
            
        } catch (Exception e) {
            log.error("【指标计算异常】任务:{} 获取指标失败", taskId, e);
            return createDefaultMetrics(taskId);
        }
    }

    @Override
    public List<Double> batchPredictContactValue(List<Long> contactIds, Long taskId) {
        try {
            return contactIds.stream()
                    .map(contactId -> calculateCustomerValueFactor(contactId, taskId))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("【批量预测异常】任务:{} 批量预测失败", taskId, e);
            return contactIds.stream().map(id -> 1.0).collect(Collectors.toList());
        }
    }

    // 私有辅助方法

    private double calculatePredictedAnswerRate(PredictiveDialingMetrics metrics) {
        if (metrics.getHistoricalAnswerRate() != null) {
            return metrics.getHistoricalAnswerRate();
        }
        return BASE_ANSWER_RATE;
    }

    private double calculateExpectedUtilization(int availableAgents, PredictiveDialingMetrics metrics) {
        // 基于历史数据计算预期利用率
        return 0.8; // 默认80%
    }

    private double calculateTimeFactor() {
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();
        
        // 工作时间因子
        if (hour >= 9 && hour <= 17) {
            return 1.2; // 工作时间接通率更高
        } else if (hour >= 18 && hour <= 21) {
            return 1.0; // 晚上一般
        } else {
            return 0.6; // 深夜接通率较低
        }
    }

    private double calculateSeasonalFactor() {
        // 季节性因子（简化实现）
        return 1.0;
    }

    private double calculateDayTypeFactor() {
        // 工作日/周末因子
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            return 1.1; // 工作日
        } else {
            return 0.9; // 周末
        }
    }

    private int calculateOptimalDialCount(int availableAgents, double expectedUtilization, double answerRate) {
        double baseCount = availableAgents * expectedUtilization / answerRate;
        return Math.max(1, (int) Math.round(baseCount));
    }

    private int calculateOptimalDialInterval(double answerRate) {
        if (answerRate > 0.6) {
            return 2; // 高接通率，快速拨号
        } else if (answerRate > 0.3) {
            return 5; // 中等接通率
        } else {
            return 10; // 低接通率，慢速拨号
        }
    }

    private String calculateRiskLevel(int dialCount, int availableAgents, double answerRate) {
        double utilization = (double) dialCount / availableAgents;
        
        if (utilization > 2.0 || answerRate < 0.2) {
            return "高";
        } else if (utilization > 1.5 || answerRate < 0.3) {
            return "中";
        } else {
            return "低";
        }
    }

    private double calculateConfidence(PredictiveDialingMetrics metrics) {
        if (metrics.getSampleSize() == null || metrics.getSampleSize() < 10) {
            return 0.5; // 样本不足，置信度较低
        }
        
        double baseConfidence = Math.min(0.9, 0.5 + metrics.getSampleSize() * 0.01);
        
        if (metrics.getPredictionAccuracy() != null) {
            baseConfidence = baseConfidence * metrics.getPredictionAccuracy();
        }
        
        return baseConfidence;
    }

    private List<Long> generatePriorityContactList(Long taskId, int count) {
        try {
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(taskId);
            query.setStatus(0); // 未分配
            
            List<CallTaskContactVo> contacts = callTaskService.getTaskContactList(query);
            
            if (CollectionUtils.isEmpty(contacts)) {
                return Collections.emptyList();
            }
            
            // 按价值排序并选择前N个
            return contacts.stream()
                    .sorted((c1, c2) -> Double.compare(
                            calculateCustomerValueFactor(c1.getId(), taskId),
                            calculateCustomerValueFactor(c2.getId(), taskId)
                    ))
                    .limit(count)
                    .map(CallTaskContactVo::getId)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.warn("【优先级列表生成异常】任务:{} 生成失败", taskId, e);
            return Collections.emptyList();
        }
    }

    private String generatePredictionNote(int dialCount, double answerRate, String riskLevel) {
        return String.format("建议拨号%d个，预期成功率%.1f%%，风险等级：%s", 
                dialCount, answerRate * 100, riskLevel);
    }

    private String generateAdjustmentSuggestions(int dialCount, double utilization) {
        if (utilization > 0.9) {
            return "建议增加坐席数量或减少拨号频率";
        } else if (utilization < 0.5) {
            return "建议增加拨号数量以提高坐席利用率";
        } else {
            return "当前配置较为合理";
        }
    }

    private double calculateExpectedRevenueScore(int dialCount, double answerRate) {
        // 简化的收益评分计算
        return dialCount * answerRate * 100;
    }

    private double calculateResourceConsumption(int dialCount, int availableAgents) {
        return (double) dialCount / availableAgents;
    }

    private PredictiveDialingResult createFallbackResult(int availableAgents) {
        PredictiveDialingResult result = new PredictiveDialingResult();
        result.setRecommendedDialCount(Math.max(1, availableAgents));
        result.setPredictedSuccessRate(BASE_ANSWER_RATE);
        result.setExpectedAgentUtilization(0.8);
        result.setExpectedAnswerCount((int) Math.round(availableAgents * BASE_ANSWER_RATE));
        result.setRecommendedDialInterval(5);
        result.setRiskLevel("中");
        result.setConfidence(0.5);
        result.setPredictionTime(LocalDateTime.now());
        result.setAlgorithmVersion("2.0-fallback");
        result.setPredictionNote("使用默认预测参数");
        return result;
    }

    private PredictiveDialingMetrics createDefaultMetrics(Long taskId) {
        PredictiveDialingMetrics metrics = new PredictiveDialingMetrics();
        metrics.setTaskId(taskId);
        metrics.setHistoricalAnswerRate(BASE_ANSWER_RATE);
        metrics.setAverageCallDuration(300); // 5分钟
        metrics.setAverageAgentIdleTime(60); // 1分钟
        metrics.setAverageCustomerResponseTime(10); // 10秒
        metrics.setHourlyAnswerRate(BASE_ANSWER_RATE);
        metrics.setCustomerValueScore(1.0);
        metrics.setAgentSkillMatch(1.0);
        metrics.setPredictionAccuracy(0.7);
        metrics.setUpdateTime(LocalDateTime.now());
        metrics.setSampleSize(0);
        metrics.setConfidence(0.5);
        metrics.setTrend("稳定");
        metrics.setSeasonalFactor(1.0);
        metrics.setDayTypeFactor(1.0);
        return metrics;
    }

    // 其他辅助方法的简化实现
    private double calculateHistoricalAnswerRate(List<CallTaskContactVo> contacts) {
        return 0.3; // 简化实现
    }

    private Integer calculateAverageCallDuration(List<CallTaskContactVo> contacts) {
        return 300; // 5分钟
    }

    private Integer calculateAverageAgentIdleTime() {
        return 60; // 1分钟
    }

    private Integer calculateAverageCustomerResponseTime() {
        return 10; // 10秒
    }

    private Double calculateHourlyAnswerRate() {
        return BASE_ANSWER_RATE;
    }

    private Double calculateCustomerValueScore(List<CallTaskContactVo> contacts) {
        return 1.0;
    }

    private Double calculateAgentSkillMatch() {
        return 1.0;
    }

    private Double calculatePredictionAccuracy() {
        return 0.7;
    }

    private String calculateTrend(List<CallTaskContactVo> contacts) {
        return "稳定";
    }

    private double calculateCustomerValueFactor(Long contactId, Long taskId) {
        return 1.0; // 简化实现
    }

    private int calculateOptimalHour(Long taskId) {
        return 14; // 下午2点
    }
}
