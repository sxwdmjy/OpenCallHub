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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预测式外呼算法服务实现（基础版本）
 * 
 * @author danmo
 * @date 2025/01/15
 */
@RequiredArgsConstructor
@Slf4j
@Service("predictiveAlgorithmService")
public class PredictiveAlgorithmServiceImpl implements IPredictiveAlgorithmService {

    private final ICallTaskService callTaskService;

    // 算法参数
    private static final double BASE_ANSWER_RATE = 0.25;
    private static final int BASE_CALL_DURATION = 300; // 5分钟
    private static final int BASE_IDLE_TIME = 60; // 1分钟
    private static final int BASE_RESPONSE_TIME = 10; // 10秒

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
        
        // 工作时间因子 (9:00-18:00为最佳时间)
        if (hour >= 9 && hour <= 18) {
            return 1.3; // 工作时间接通率更高
        } else if (hour >= 19 && hour <= 21) {
            return 1.0; // 晚上一般
        } else {
            return 0.6; // 深夜接通率较低
        }
    }

    private double calculateSeasonalFactor() {
        // 季节性因子（基于月份）
        int month = LocalDateTime.now().getMonthValue();
        if (month >= 3 && month <= 5) { // 春季
            return 1.1;
        } else if (month >= 6 && month <= 8) { // 夏季
            return 1.0;
        } else if (month >= 9 && month <= 11) { // 秋季
            return 1.05;
        } else { // 冬季
            return 0.95;
        }
    }

    private double calculateDayTypeFactor() {
        // 工作日/周末因子
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            return 1.2; // 工作日
        } else {
            return 0.8; // 周末
        }
    }

    private int calculateOptimalDialCount(int availableAgents, double expectedUtilization, double answerRate) {
        // 使用更精确的计算公式
        // 拨号数量 = (坐席数量 * 目标利用率) / 预测接通率
        double baseCount = (availableAgents * expectedUtilization) / answerRate;
        
        // 添加缓冲系数，避免过度拨号
        double bufferFactor = 1.1; // 10%缓冲
        
        return Math.max(1, (int) Math.ceil(baseCount * bufferFactor));
    }

    private int calculateOptimalDialInterval(double answerRate) {
        // 根据接通率动态调整拨号间隔
        if (answerRate > 0.7) {
            return 1; // 高接通率，快速拨号
        } else if (answerRate > 0.5) {
            return 3; // 中高接通率
        } else if (answerRate > 0.3) {
            return 5; // 中等接通率
        } else {
            return 10; // 低接通率，慢速拨号
        }
    }

    private String calculateRiskLevel(int dialCount, int availableAgents, double answerRate) {
        double utilization = (double) dialCount / availableAgents;
        
        // 综合考虑利用率和接通率
        if (utilization > 2.5 || answerRate < 0.15) {
            return "高";
        } else if (utilization > 1.8 || answerRate < 0.25) {
            return "中";
        } else {
            return "低";
        }
    }

    private double calculateConfidence(PredictiveDialingMetrics metrics) {
        if (metrics.getSampleSize() == null || metrics.getSampleSize() < 5) {
            return 0.3; // 样本不足，置信度较低
        }
        
        // 基于样本数量计算基础置信度
        double baseConfidence = Math.min(0.95, 0.3 + metrics.getSampleSize() * 0.02);
        
        if (metrics.getPredictionAccuracy() != null) {
            baseConfidence = baseConfidence * metrics.getPredictionAccuracy();
        }
        
        return Math.max(0.1, baseConfidence); // 最低置信度10%
    }

    private List<Long> generatePriorityContactList(Long taskId, int count) {
        try {
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(taskId);
            query.setStatus(0); // 未分配
            query.setCallStatus(0); // 未拨打
            
            List<CallTaskContactVo> contacts = callTaskService.getTaskContactList(query);
            
            if (CollectionUtils.isEmpty(contacts)) {
                return Collections.emptyList();
            }
            
            // 按价值排序并选择前N个
            return contacts.stream()
                    .sorted((c1, c2) -> {
                        // 综合评分 = 客户价值 * 0.4 + 历史接通率 * 0.6
                        double score1 = calculateCustomerValueFactor(c1.getId(), taskId) * 0.4 + 
                                      (c1.getCallStatus() != null && c1.getCallStatus() == 1 ? 1.0 : 0.2) * 0.6;
                        double score2 = calculateCustomerValueFactor(c2.getId(), taskId) * 0.4 + 
                                      (c2.getCallStatus() != null && c2.getCallStatus() == 1 ? 1.0 : 0.2) * 0.6;
                        return Double.compare(score2, score1);
                    })
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
        if (utilization > 1.0) {
            return "建议增加坐席数量或减少拨号频率";
        } else if (utilization < 0.6) {
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
        metrics.setAverageCallDuration(BASE_CALL_DURATION);
        metrics.setAverageAgentIdleTime(BASE_IDLE_TIME);
        metrics.setAverageCustomerResponseTime(BASE_RESPONSE_TIME);
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

    // 基于历史数据计算真实指标
    private double calculateHistoricalAnswerRate(List<CallTaskContactVo> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return BASE_ANSWER_RATE;
        }
        
        long answeredCount = contacts.stream()
                .filter(contact -> contact.getCallStatus() != null && contact.getCallStatus() == 1)
                .count();
        
        return (double) answeredCount / contacts.size();
    }

    private Integer calculateAverageCallDuration(List<CallTaskContactVo> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return BASE_CALL_DURATION;
        }
        
        // 简化实现：假设已接通的通话平均时长为300秒
        long answeredCount = contacts.stream()
                .filter(contact -> contact.getCallStatus() != null && contact.getCallStatus() == 1)
                .count();
        
        if (answeredCount == 0) {
            return BASE_CALL_DURATION;
        }
        
        // 假设平均通话时长为300秒
        return 300;
    }

    private Integer calculateAverageAgentIdleTime() {
        // 返回默认值
        return BASE_IDLE_TIME;
    }

    private Integer calculateAverageCustomerResponseTime() {
        // 返回默认值
        return BASE_RESPONSE_TIME;
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
        return 0.75; // 默认准确率75%
    }

    private String calculateTrend(List<CallTaskContactVo> contacts) {
        // 简化实现：根据最近通话情况判断趋势
        if (CollectionUtils.isEmpty(contacts)) {
            return "稳定";
        }
        
        // 判断最近通话的趋势（这里简化处理）
        return "稳定";
    }

    private double calculateCustomerValueFactor(Long contactId, Long taskId) {
        // 简化实现：基于客户信息计算价值因子
        return 1.0;
    }

    private int calculateOptimalHour(Long taskId) {
        // 简化实现：最佳拨打时间为下午2点
        return 14;
    }
}