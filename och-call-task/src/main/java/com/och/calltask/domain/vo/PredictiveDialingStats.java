package com.och.calltask.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预测式外呼统计信息
 * 
 * @author danmo
 * @date 2025/01/15
 */
@Data
public class PredictiveDialingStats {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 统计时间段
     */
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    /**
     * 基础统计
     */
    private BasicStats basicStats;
    
    /**
     * 性能统计
     */
    private PerformanceStats performanceStats;
    
    /**
     * 预测统计
     */
    private PredictionStats predictionStats;
    
    /**
     * 客户分析
     */
    private CustomerAnalysis customerAnalysis;
    
    /**
     * 坐席分析
     */
    private AgentAnalysis agentAnalysis;
    
    /**
     * 时间分析
     */
    private TimeAnalysis timeAnalysis;

    @Data
    public static class BasicStats {
        /**
         * 总拨号数
         */
        private Integer totalDialCount;
        
        /**
         * 接通数
         */
        private Integer answerCount;
        
        /**
         * 未接通数
         */
        private Integer noAnswerCount;
        
        /**
         * 忙音数
         */
        private Integer busyCount;
        
        /**
         * 拒接数
         */
        private Integer rejectCount;
        
        /**
         * 成功率
         */
        private Double successRate;
        
        /**
         * 平均通话时长（秒）
         */
        private Integer averageCallDuration;
        
        /**
         * 总通话时长（秒）
         */
        private Long totalCallDuration;
    }

    @Data
    public static class PerformanceStats {
        /**
         * 平均拨号间隔（秒）
         */
        private Double averageDialInterval;
        
        /**
         * 坐席平均利用率
         */
        private Double averageAgentUtilization;
        
        /**
         * 系统响应时间（毫秒）
         */
        private Long averageResponseTime;
        
        /**
         * 错误率
         */
        private Double errorRate;
        
        /**
         * 重试次数
         */
        private Integer retryCount;
        
        /**
         * 超时次数
         */
        private Integer timeoutCount;
    }

    @Data
    public static class PredictionStats {
        /**
         * 预测准确率
         */
        private Double predictionAccuracy;
        
        /**
         * 预测成功率
         */
        private Double predictedSuccessRate;
        
        /**
         * 实际成功率
         */
        private Double actualSuccessRate;
        
        /**
         * 预测偏差
         */
        private Double predictionDeviation;
        
        /**
         * 置信度
         */
        private Double confidence;
        
        /**
         * 模型版本
         */
        private String modelVersion;
        
        /**
         * 预测更新次数
         */
        private Integer predictionUpdateCount;
    }

    @Data
    public static class CustomerAnalysis {
        /**
         * 客户接通率分布
         */
        private Map<String, Double> answerRateDistribution;
        
        /**
         * 高价值客户接通率
         */
        private Double highValueCustomerAnswerRate;
        
        /**
         * 客户响应时间分布
         */
        private Map<String, Integer> responseTimeDistribution;
        
        /**
         * 客户满意度评分
         */
        private Double customerSatisfactionScore;
        
        /**
         * 客户投诉数
         */
        private Integer customerComplaintCount;
    }

    @Data
    public static class AgentAnalysis {
        /**
         * 坐席效率排名
         */
        private List<AgentEfficiency> agentEfficiencyRanking;
        
        /**
         * 坐席技能匹配度
         */
        private Double averageSkillMatch;
        
        /**
         * 坐席工作负载分布
         */
        private Map<String, Integer> workloadDistribution;
        
        /**
         * 坐席满意度
         */
        private Double agentSatisfactionScore;
        
        /**
         * 坐席培训需求
         */
        private List<String> trainingNeeds;
    }

    @Data
    public static class AgentEfficiency {
        /**
         * 坐席ID
         */
        private Long agentId;
        
        /**
         * 坐席姓名
         */
        private String agentName;
        
        /**
         * 效率评分
         */
        private Double efficiencyScore;
        
        /**
         * 接通数
         */
        private Integer answerCount;
        
        /**
         * 平均通话时长
         */
        private Integer averageCallDuration;
    }

    @Data
    public static class TimeAnalysis {
        /**
         * 按小时的成功率分布
         */
        private Map<Integer, Double> hourlySuccessRate;
        
        /**
         * 按天的成功率分布
         */
        private Map<String, Double> dailySuccessRate;
        
        /**
         * 最佳拨号时间段
         */
        private List<Integer> optimalDialHours;
        
        /**
         * 最差拨号时间段
         */
        private List<Integer> worstDialHours;
        
        /**
         * 季节性影响因子
         */
        private Double seasonalImpactFactor;
    }
}
