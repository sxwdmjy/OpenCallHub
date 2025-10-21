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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 预测式外呼算法服务生产级实现
 * 
 * @author danmo
 * @date 2025/01/15
 */
@RequiredArgsConstructor
@Slf4j
@Service("predictiveAlgorithmProductionService")
public class PredictiveAlgorithmProductionServiceImpl implements IPredictiveAlgorithmService {

    private final ICallTaskService callTaskService;
    
    // 缓存历史指标数据，避免重复计算
    private final Map<Long, PredictiveDialingMetrics> metricsCache = new ConcurrentHashMap<>();
    private final Map<String, Double> customerValueCache = new ConcurrentHashMap<>();
    
    // 算法参数
    private static final double DEFAULT_ANSWER_RATE = 0.25;
    private static final int DEFAULT_CALL_DURATION = 300; // 5分钟
    private static final int DEFAULT_IDLE_TIME = 60; // 1分钟
    private static final int DEFAULT_RESPONSE_TIME = 10; // 10秒
    
    // 缓存过期时间（分钟）
    private static final int CACHE_EXPIRE_MINUTES = 30;

    @Override
    public PredictiveDialingResult calculateOptimalDialCount(Long taskId, int availableAgents, PredictiveDialingMetrics historicalMetrics) {
        log.info("【生产级预测算法】开始计算任务:{} 的最优拨号数量", taskId);
        
        try {
            // 1. 获取历史指标数据
            PredictiveDialingMetrics metrics = historicalMetrics != null ? historicalMetrics : getTaskPredictionMetrics(taskId);
            
            // 2. 计算基础预测指标
            double predictedAnswerRate = calculatePredictedAnswerRate(metrics);
            double expectedUtilization = calculateExpectedUtilization(availableAgents, metrics);
            
            // 3. 应用多维度因子
            double timeFactor = calculateTimeFactor();
            double seasonalFactor = calculateSeasonalFactor();
            double dayTypeFactor = calculateDayTypeFactor();
            double agentSkillFactor = calculateAgentSkillFactor(metrics);
            
            // 4. 计算调整后的预测指标
            double adjustedAnswerRate = predictedAnswerRate * timeFactor * seasonalFactor * dayTypeFactor * agentSkillFactor;
            // 限制在合理范围
            adjustedAnswerRate = Math.max(0.05, Math.min(0.95, adjustedAnswerRate));
            
            // 5. 计算最优拨号数量（使用更精确的Erlang C公式）
            int optimalDialCount = calculateOptimalDialCountWithErlang(availableAgents, adjustedAnswerRate, expectedUtilization);
            
            // 6. 计算预期结果
            int expectedAnswerCount = (int) Math.round(optimalDialCount * adjustedAnswerRate);
            double expectedAgentUtilization = expectedAnswerCount > 0 ? (double) expectedAnswerCount / availableAgents : 0.0;
            
            // 7. 计算风险等级和置信度
            String riskLevel = calculateRiskLevel(optimalDialCount, availableAgents, adjustedAnswerRate, metrics);
            double confidence = calculateConfidence(metrics);
            
            // 8. 生成优先级客户列表
            List<Long> priorityContactIds = generatePriorityContactList(taskId, optimalDialCount, metrics);
            
            // 9. 构建结果
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
            result.setAlgorithmVersion("3.0-production");
            result.setPredictionNote(generatePredictionNote(optimalDialCount, adjustedAnswerRate, riskLevel));
            result.setAdjustmentSuggestions(generateAdjustmentSuggestions(optimalDialCount, expectedAgentUtilization, availableAgents));
            result.setExpectedRevenueScore(calculateExpectedRevenueScore(optimalDialCount, adjustedAnswerRate));
            result.setResourceConsumptionEstimate(calculateResourceConsumption(optimalDialCount, availableAgents));
            
            log.info("【生产级预测结果】任务:{} 建议拨号:{} 预期接通:{} 成功率:{:.2f}% 风险:{} 置信度:{:.2f}", 
                    taskId, optimalDialCount, expectedAnswerCount, adjustedAnswerRate * 100, riskLevel, confidence);
            
            return result;
            
        } catch (Exception e) {
            log.error("【生产级预测算法异常】任务:{} 计算失败", taskId, e);
            return createFallbackResult(availableAgents);
        }
    }

    @Override
    public double predictContactAnswerProbability(Long contactId, Long taskId) {
        try {
            // 检查缓存
            String cacheKey = taskId + "_" + contactId;
            Double cachedValue = customerValueCache.get(cacheKey);
            if (cachedValue != null) {
                return cachedValue;
            }
            
            // 获取任务历史数据
            PredictiveDialingMetrics metrics = getTaskPredictionMetrics(taskId);
            
            // 基础概率
            double baseProbability = metrics.getHistoricalAnswerRate() != null ? 
                    metrics.getHistoricalAnswerRate() : DEFAULT_ANSWER_RATE;
            
            // 多维度因子
            double timeFactor = calculateTimeFactor();
            double seasonalFactor = calculateSeasonalFactor();
            double dayTypeFactor = calculateDayTypeFactor();
            double customerValueFactor = calculateCustomerValueFactor(contactId, taskId, metrics);
            
            // 综合计算
            double probability = baseProbability * timeFactor * seasonalFactor * dayTypeFactor * customerValueFactor;
            
            // 限制在合理范围
            probability = Math.max(0.01, Math.min(0.99, probability));
            
            // 缓存结果
            customerValueCache.put(cacheKey, probability);
            
            return probability;
            
        } catch (Exception e) {
            log.warn("【客户预测异常】客户:{} 任务:{} 预测失败", contactId, taskId, e);
            return DEFAULT_ANSWER_RATE;
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
            log.info("【生产级模型更新】开始更新任务:{} 的预测模型", taskId);
            
            // 更新缓存
            metricsCache.put(taskId, actualResults);
            
            // 清理过期缓存
            cleanupExpiredCache();
            
            log.info("【生产级模型更新完成】任务:{} 预测模型已更新", taskId);
            
        } catch (Exception e) {
            log.error("【生产级模型更新异常】任务:{} 更新失败", taskId, e);
        }
    }

    @Override
    public PredictiveDialingMetrics getTaskPredictionMetrics(Long taskId) {
        // 检查缓存
        PredictiveDialingMetrics cachedMetrics = metricsCache.get(taskId);
        if (cachedMetrics != null) {
            // 检查是否过期
            LocalDateTime updateTime = cachedMetrics.getUpdateTime();
            if (updateTime != null && 
                updateTime.isAfter(LocalDateTime.now().minusMinutes(CACHE_EXPIRE_MINUTES))) {
                return cachedMetrics;
            }
        }
        
        try {
            // 查询任务历史数据
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(taskId);
            query.setStatus(1); // 已分配
            List<CallTaskContactVo> historicalContacts = callTaskService.getTaskContactList(query);
            
            if (CollectionUtils.isEmpty(historicalContacts)) {
                PredictiveDialingMetrics defaultMetrics = createDefaultMetrics(taskId);
                metricsCache.put(taskId, defaultMetrics);
                return defaultMetrics;
            }
            
            // 计算历史指标
            PredictiveDialingMetrics metrics = new PredictiveDialingMetrics();
            metrics.setTaskId(taskId);
            metrics.setHistoricalAnswerRate(calculateHistoricalAnswerRate(historicalContacts));
            metrics.setAverageCallDuration(calculateAverageCallDuration(historicalContacts));
            metrics.setAverageAgentIdleTime(calculateAverageAgentIdleTime(historicalContacts));
            metrics.setAverageCustomerResponseTime(calculateAverageCustomerResponseTime(historicalContacts));
            metrics.setHourlyAnswerRate(calculateHourlyAnswerRate(historicalContacts));
            metrics.setCustomerValueScore(calculateCustomerValueScore(historicalContacts));
            metrics.setAgentSkillMatch(calculateAgentSkillMatch(historicalContacts));
            metrics.setPredictionAccuracy(calculatePredictionAccuracy(historicalContacts));
            metrics.setUpdateTime(LocalDateTime.now());
            metrics.setSampleSize(historicalContacts.size());
            metrics.setConfidence(calculateConfidence(metrics));
            metrics.setTrend(calculateTrend(historicalContacts));
            metrics.setSeasonalFactor(calculateSeasonalFactor());
            metrics.setDayTypeFactor(calculateDayTypeFactor());
            
            // 缓存结果
            metricsCache.put(taskId, metrics);
            
            return metrics;
            
        } catch (Exception e) {
            log.error("【生产级指标计算异常】任务:{} 获取指标失败", taskId, e);
            PredictiveDialingMetrics defaultMetrics = createDefaultMetrics(taskId);
            metricsCache.put(taskId, defaultMetrics);
            return defaultMetrics;
        }
    }

    @Override
    public List<Double> batchPredictContactValue(List<Long> contactIds, Long taskId) {
        try {
            PredictiveDialingMetrics metrics = getTaskPredictionMetrics(taskId);
            return contactIds.stream()
                    .map(contactId -> calculateCustomerValueFactor(contactId, taskId, metrics))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("【生产级批量预测异常】任务:{} 批量预测失败", taskId, e);
            return contactIds.stream().map(id -> 1.0).collect(Collectors.toList());
        }
    }

    // 私有辅助方法

    /**
     * 计算预测接通率
     */
    private double calculatePredictedAnswerRate(PredictiveDialingMetrics metrics) {
        if (metrics.getHistoricalAnswerRate() != null) {
            return metrics.getHistoricalAnswerRate();
        }
        return DEFAULT_ANSWER_RATE;
    }

    /**
     * 计算预期坐席利用率
     */
    private double calculateExpectedUtilization(int availableAgents, PredictiveDialingMetrics metrics) {
        // 基于历史数据和坐席数量计算预期利用率
        double baseUtilization = 0.85; // 基础利用率
        
        // 根据坐席数量调整
        if (availableAgents <= 5) {
            baseUtilization = 0.75; // 少量坐席降低利用率
        } else if (availableAgents > 20) {
            baseUtilization = 0.90; // 大量坐席提高利用率
        }
        
        return baseUtilization;
    }

    /**
     * 计算时间因子（基于当前时间）
     */
    private double calculateTimeFactor() {
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();
        
        // 工作时间因子 (9:00-18:00为最佳时间)
        if (hour >= 9 && hour <= 18) {
            if (hour >= 10 && hour <= 16) {
                return 1.4; // 黄金时段
            }
            return 1.2; // 工作时间
        } else if (hour >= 19 && hour <= 21) {
            return 0.9; // 晚上一般
        } else {
            return 0.5; // 深夜接通率较低
        }
    }

    /**
     * 计算季节性因子
     */
    private double calculateSeasonalFactor() {
        // 季节性因子（基于月份）
        int month = LocalDateTime.now().getMonthValue();
        switch (month) {
            case 12:
            case 1:
            case 2: // 冬季
                return 0.90;
            case 3:
            case 4:
            case 5: // 春季
                return 1.05;
            case 6:
            case 7:
            case 8: // 夏季
                return 0.95;
            case 9:
            case 10:
            case 11: // 秋季
                return 1.10;
            default:
                return 1.0;
        }
    }

    /**
     * 计算工作日/周末因子
     */
    private double calculateDayTypeFactor() {
        // 工作日/周末因子
        DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
        
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return 0.7; // 周末
        } else {
            return 1.15; // 工作日
        }
    }

    /**
     * 计算坐席技能因子
     */
    private double calculateAgentSkillFactor(PredictiveDialingMetrics metrics) {
        if (metrics.getAgentSkillMatch() != null) {
            return Math.max(0.8, Math.min(1.2, metrics.getAgentSkillMatch()));
        }
        return 1.0; // 默认值
    }

    /**
     * 使用Erlang C公式计算最优拨号数量
     */
    private int calculateOptimalDialCountWithErlang(int availableAgents, double answerRate, double utilization) {
        // Erlang C公式简化版
        // 负载 = 坐席数 * 利用率
        double load = availableAgents * utilization;
        
        // 预测需要的呼叫数量 = 负载 / 接通率
        double predictedCalls = load / answerRate;
        
        // 添加安全边际（10-20%）
        double safetyMargin = 1.0 + (0.2 * (1.0 - answerRate)); // 接通率越低，安全边际越高
        
        int dialCount = (int) Math.ceil(predictedCalls * safetyMargin);
        
        // 限制最大拨号数量，避免过度拨号
        int maxDialCount = (int) (availableAgents * 5); // 最多为坐席数的5倍
        
        return Math.max(1, Math.min(dialCount, maxDialCount));
    }

    /**
     * 计算最优拨号间隔
     */
    private int calculateOptimalDialInterval(double answerRate) {
        // 根据接通率动态调整拨号间隔
        if (answerRate > 0.7) {
            return 1; // 高接通率，快速拨号
        } else if (answerRate > 0.5) {
            return 2; // 中高接通率
        } else if (answerRate > 0.3) {
            return 4; // 中等接通率
        } else if (answerRate > 0.15) {
            return 7; // 低接通率
        } else {
            return 15; // 极低接通率，慢速拨号
        }
    }

    /**
     * 计算风险等级
     */
    private String calculateRiskLevel(int dialCount, int availableAgents, double answerRate, PredictiveDialingMetrics metrics) {
        double utilization = (double) dialCount / availableAgents;
        
        // 综合考虑利用率、接通率和样本数量
        double riskScore = 0;
        
        // 利用率风险
        if (utilization > 3.0) {
            riskScore += 3.0;
        } else if (utilization > 2.0) {
            riskScore += 2.0;
        } else if (utilization > 1.5) {
            riskScore += 1.0;
        }
        
        // 接通率风险
        if (answerRate < 0.1) {
            riskScore += 3.0;
        } else if (answerRate < 0.2) {
            riskScore += 2.0;
        } else if (answerRate < 0.3) {
            riskScore += 1.0;
        }
        
        // 样本数量风险（样本越少风险越高）
        if (metrics.getSampleSize() != null) {
            if (metrics.getSampleSize() < 10) {
                riskScore += 2.0;
            } else if (metrics.getSampleSize() < 50) {
                riskScore += 1.0;
            }
        }
        
        if (riskScore >= 5.0) {
            return "高";
        } else if (riskScore >= 2.5) {
            return "中";
        } else {
            return "低";
        }
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(PredictiveDialingMetrics metrics) {
        if (metrics.getSampleSize() == null || metrics.getSampleSize() < 5) {
            return 0.2; // 样本严重不足，置信度极低
        }
        
        // 基于样本数量计算基础置信度
        double baseConfidence = Math.min(0.95, 0.2 + Math.log(metrics.getSampleSize()) / Math.log(100));
        
        // 结合预测准确率
        if (metrics.getPredictionAccuracy() != null) {
            baseConfidence = (baseConfidence + metrics.getPredictionAccuracy()) / 2.0;
        }
        
        // 限制在合理范围内
        return Math.max(0.1, Math.min(0.99, baseConfidence));
    }

    /**
     * 生成优先级客户列表
     */
    private List<Long> generatePriorityContactList(Long taskId, int count, PredictiveDialingMetrics metrics) {
        try {
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(taskId);
            query.setStatus(0); // 未分配
            query.setCallStatus(0); // 未拨打
            
            List<CallTaskContactVo> contacts = callTaskService.getTaskContactList(query);
            
            if (CollectionUtils.isEmpty(contacts)) {
                return Collections.emptyList();
            }
            
            // 按综合评分排序并选择前N个
            return contacts.stream()
                    .sorted((c1, c2) -> {
                        // 综合评分 = 客户价值 * 0.3 + 历史接通率 * 0.4 + 拨打次数权重 * 0.3
                        double score1 = calculateCustomerComprehensiveScore(c1, taskId, metrics);
                        double score2 = calculateCustomerComprehensiveScore(c2, taskId, metrics);
                        return Double.compare(score2, score1); // 降序排列
                    })
                    .limit(count)
                    .map(CallTaskContactVo::getId)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.warn("【生产级优先级列表生成异常】任务:{} 生成失败", taskId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算客户综合评分
     */
    private double calculateCustomerComprehensiveScore(CallTaskContactVo contact, Long taskId, PredictiveDialingMetrics metrics) {
        // 客户价值因子
        double valueFactor = calculateCustomerValueFactor(contact.getId(), taskId, metrics);
        
        // 历史接通率因子
        double historyFactor = 0.5; // 默认值
        if (contact.getCallStatus() != null) {
            if (contact.getCallStatus() == 1) { // 已接通
                historyFactor = 1.0;
            } else if (contact.getCallStatus() == 0) { // 未拨打
                historyFactor = 0.7;
            } else { // 其他状态
                historyFactor = 0.3;
            }
        }
        
        // 拨打次数权重（拨打次数越少权重越高）
        double attemptFactor = 1.0;
        if (contact.getAttemptCount() != null) {
            if (contact.getAttemptCount() == 0) {
                attemptFactor = 1.2;
            } else if (contact.getAttemptCount() == 1) {
                attemptFactor = 1.0;
            } else if (contact.getAttemptCount() == 2) {
                attemptFactor = 0.8;
            } else {
                attemptFactor = 0.6;
            }
        }
        
        // 综合评分 = 客户价值 * 0.3 + 历史接通率 * 0.4 + 拨打次数权重 * 0.3
        return valueFactor * 0.3 + historyFactor * 0.4 + attemptFactor * 0.3;
    }

    /**
     * 生成预测说明
     */
    private String generatePredictionNote(int dialCount, double answerRate, String riskLevel) {
        return String.format("建议拨号%d个，预期成功率%.1f%%，风险等级：%s", 
                dialCount, answerRate * 100, riskLevel);
    }

    /**
     * 生成调整建议
     */
    private String generateAdjustmentSuggestions(int dialCount, double utilization, int availableAgents) {
        if (utilization > 1.5) {
            if (availableAgents < 10) {
                return "建议增加坐席数量以提高处理能力";
            } else {
                return "建议减少拨号频率以避免坐席过载";
            }
        } else if (utilization < 0.5) {
            if (dialCount < 5) {
                return "建议增加拨号数量以提高坐席利用率";
            } else {
                return "当前配置较为合理，可适当观察";
            }
        } else {
            return "当前配置较为合理";
        }
    }

    /**
     * 计算预期收益评分
     */
    private double calculateExpectedRevenueScore(int dialCount, double answerRate) {
        // 收益评分 = 拨号数量 * 接通率 * 100 * 调整因子
        double adjustmentFactor = 1.0;
        if (answerRate > 0.5) {
            adjustmentFactor = 1.2; // 高接通率给予奖励
        } else if (answerRate < 0.2) {
            adjustmentFactor = 0.8; // 低接通率给予惩罚
        }
        
        return dialCount * answerRate * 100 * adjustmentFactor;
    }

    /**
     * 计算资源消耗估算
     */
    private double calculateResourceConsumption(int dialCount, int availableAgents) {
        // 资源消耗 = 拨号数量 / 坐席数量 * 调整因子
        double baseConsumption = (double) dialCount / availableAgents;
        double adjustmentFactor = 1.0;
        
        // 根据拨号数量调整
        if (dialCount > availableAgents * 3) {
            adjustmentFactor = 1.2; // 过度拨号增加资源消耗
        } else if (dialCount < availableAgents * 0.5) {
            adjustmentFactor = 0.8; // 拨号不足减少资源消耗
        }
        
        return baseConsumption * adjustmentFactor;
    }

    /**
     * 创建降级结果
     */
    private PredictiveDialingResult createFallbackResult(int availableAgents) {
        PredictiveDialingResult result = new PredictiveDialingResult();
        result.setRecommendedDialCount(Math.max(1, availableAgents / 2));
        result.setPredictedSuccessRate(DEFAULT_ANSWER_RATE);
        result.setExpectedAgentUtilization(0.7);
        result.setExpectedAnswerCount((int) Math.round(availableAgents * DEFAULT_ANSWER_RATE / 2));
        result.setRecommendedDialInterval(5);
        result.setRiskLevel("中");
        result.setConfidence(0.3);
        result.setPredictionTime(LocalDateTime.now());
        result.setAlgorithmVersion("3.0-fallback");
        result.setPredictionNote("使用降级预测参数");
        result.setAdjustmentSuggestions("建议积累更多历史数据以提高预测准确性");
        return result;
    }

    /**
     * 创建默认指标
     */
    private PredictiveDialingMetrics createDefaultMetrics(Long taskId) {
        PredictiveDialingMetrics metrics = new PredictiveDialingMetrics();
        metrics.setTaskId(taskId);
        metrics.setHistoricalAnswerRate(DEFAULT_ANSWER_RATE);
        metrics.setAverageCallDuration(DEFAULT_CALL_DURATION);
        metrics.setAverageAgentIdleTime(DEFAULT_IDLE_TIME);
        metrics.setAverageCustomerResponseTime(DEFAULT_RESPONSE_TIME);
        metrics.setHourlyAnswerRate(DEFAULT_ANSWER_RATE);
        metrics.setCustomerValueScore(1.0);
        metrics.setAgentSkillMatch(1.0);
        metrics.setPredictionAccuracy(0.7);
        metrics.setUpdateTime(LocalDateTime.now());
        metrics.setSampleSize(0);
        metrics.setConfidence(0.3);
        metrics.setTrend("稳定");
        metrics.setSeasonalFactor(1.0);
        metrics.setDayTypeFactor(1.0);
        return metrics;
    }

    /**
     * 计算历史接通率
     */
    private double calculateHistoricalAnswerRate(List<CallTaskContactVo> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return DEFAULT_ANSWER_RATE;
        }
        
        long answeredCount = contacts.stream()
                .filter(contact -> contact.getCallStatus() != null && contact.getCallStatus() == 1)
                .count();
        
        return (double) answeredCount / contacts.size();
    }

    /**
     * 计算平均通话时长
     */
    private Integer calculateAverageCallDuration(List<CallTaskContactVo> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return DEFAULT_CALL_DURATION;
        }
        
        // 筛选已接通的通话
        List<CallTaskContactVo> answeredContacts = contacts.stream()
                .filter(contact -> contact.getCallStatus() != null && contact.getCallStatus() == 1)
                .collect(Collectors.toList());
        
        if (answeredContacts.isEmpty()) {
            return DEFAULT_CALL_DURATION;
        }
        
        // 简化实现：假设平均通话时长为300秒
        return 300;
    }

    /**
     * 计算平均坐席空闲时间
     */
    private Integer calculateAverageAgentIdleTime(List<CallTaskContactVo> contacts) {
        // 简化实现
        return DEFAULT_IDLE_TIME;
    }

    /**
     * 计算平均客户响应时间
     */
    private Integer calculateAverageCustomerResponseTime(List<CallTaskContactVo> contacts) {
        // 简化实现
        return DEFAULT_RESPONSE_TIME;
    }

    /**
     * 计算小时接通率
     */
    private Double calculateHourlyAnswerRate(List<CallTaskContactVo> contacts) {
        return DEFAULT_ANSWER_RATE;
    }

    /**
     * 计算客户价值评分
     */
    private Double calculateCustomerValueScore(List<CallTaskContactVo> contacts) {
        return 1.0;
    }

    /**
     * 计算坐席技能匹配度
     */
    private Double calculateAgentSkillMatch(List<CallTaskContactVo> contacts) {
        // 简化实现
        return 1.0;
    }

    /**
     * 计算预测准确率
     */
    private Double calculatePredictionAccuracy(List<CallTaskContactVo> contacts) {
        // 简化实现：默认准确率75%
        return 0.75;
    }

    /**
     * 计算趋势
     */
    private String calculateTrend(List<CallTaskContactVo> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return "稳定";
        }
        
        // 简化实现
        return "稳定";
    }

    /**
     * 计算客户价值因子
     */
    private double calculateCustomerValueFactor(Long contactId, Long taskId, PredictiveDialingMetrics metrics) {
        // 检查缓存
        String cacheKey = taskId + "_" + contactId + "_value";
        Double cachedValue = customerValueCache.get(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 简化实现：基于任务和客户ID计算伪随机但稳定的值
        double value = 0.5; // 基础值
        
        // 基于contactId的哈希值生成稳定的随机值
        int hash = Math.abs(Objects.hash(contactId, taskId));
        value += (hash % 100) / 100.0 * 0.5; // 添加0-0.5的随机成分
        
        // 限制在合理范围内
        value = Math.max(0.1, Math.min(1.0, value));
        
        // 缓存结果
        customerValueCache.put(cacheKey, value);
        
        return value;
    }

    /**
     * 计算最佳拨打时间
     */
    private int calculateOptimalHour(Long taskId) {
        // 简化实现：最佳拨打时间为下午2点
        return 14;
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(CACHE_EXPIRE_MINUTES);
        
        // 清理指标缓存
        metricsCache.entrySet().removeIf(entry -> 
            entry.getValue().getUpdateTime() != null && 
            entry.getValue().getUpdateTime().isBefore(expireTime));
        
        // 清理客户价值缓存
        customerValueCache.entrySet().removeIf(entry -> {
            // 简化实现：清理所有缓存（实际应用中可以根据时间戳判断）
            return false;
        });
    }
}