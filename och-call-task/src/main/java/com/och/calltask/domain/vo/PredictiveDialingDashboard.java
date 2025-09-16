package com.och.calltask.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预测式外呼监控仪表板
 * 
 * @author danmo
 * @date 2025/01/15
 */
@Data
public class PredictiveDialingDashboard {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 当前状态
     */
    private String currentStatus;
    
    /**
     * 实时指标
     */
    private RealtimeMetrics realtimeMetrics;
    
    /**
     * 性能趋势
     */
    private PerformanceTrend performanceTrend;
    
    /**
     * 坐席状态
     */
    private AgentStatus agentStatus;
    
    /**
     * 风险预警
     */
    private List<String> riskAlerts;
    
    /**
     * 优化建议
     */
    private List<String> optimizationSuggestions;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
    
    /**
     * 数据质量评分
     */
    private Double dataQualityScore;
    
    /**
     * 系统健康状态
     */
    private String systemHealthStatus;

    @Data
    public static class RealtimeMetrics {
        /**
         * 当前拨号数量
         */
        private Integer currentDialCount;
        
        /**
         * 当前接通数量
         */
        private Integer currentAnswerCount;
        
        /**
         * 实时成功率
         */
        private Double realtimeSuccessRate;
        
        /**
         * 坐席利用率
         */
        private Double agentUtilization;
        
        /**
         * 平均等待时间（秒）
         */
        private Integer averageWaitTime;
        
        /**
         * 预测准确率
         */
        private Double predictionAccuracy;
        
        /**
         * 队列长度
         */
        private Integer queueLength;
        
        /**
         * 系统负载
         */
        private Double systemLoad;
    }

    @Data
    public static class PerformanceTrend {
        /**
         * 成功率趋势（最近24小时）
         */
        private List<Double> successRateTrend;
        
        /**
         * 拨号数量趋势
         */
        private List<Integer> dialCountTrend;
        
        /**
         * 坐席利用率趋势
         */
        private List<Double> utilizationTrend;
        
        /**
         * 预测准确率趋势
         */
        private List<Double> accuracyTrend;
        
        /**
         * 时间标签
         */
        private List<String> timeLabels;
    }

    @Data
    public static class AgentStatus {
        /**
         * 总坐席数
         */
        private Integer totalAgents;
        
        /**
         * 在线坐席数
         */
        private Integer onlineAgents;
        
        /**
         * 空闲坐席数
         */
        private Integer idleAgents;
        
        /**
         * 忙碌坐席数
         */
        private Integer busyAgents;
        
        /**
         * 坐席状态分布
         */
        private Map<String, Integer> agentStatusDistribution;
        
        /**
         * 平均通话时长（秒）
         */
        private Integer averageCallDuration;
        
        /**
         * 坐席效率评分
         */
        private Double agentEfficiencyScore;
    }
}
