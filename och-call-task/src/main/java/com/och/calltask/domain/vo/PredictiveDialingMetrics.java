package com.och.calltask.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测式外呼指标
 * 
 * @author danmo
 * @date 2025/01/15
 */
@Data
public class PredictiveDialingMetrics {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 历史接通率
     */
    private Double historicalAnswerRate;
    
    /**
     * 平均通话时长（秒）
     */
    private Integer averageCallDuration;
    
    /**
     * 坐席平均空闲时间（秒）
     */
    private Integer averageAgentIdleTime;
    
    /**
     * 客户平均响应时间（秒）
     */
    private Integer averageCustomerResponseTime;
    
    /**
     * 时间段接通率（按小时）
     */
    private Double hourlyAnswerRate;
    
    /**
     * 客户价值评分
     */
    private Double customerValueScore;
    
    /**
     * 坐席技能匹配度
     */
    private Double agentSkillMatch;
    
    /**
     * 预测准确率
     */
    private Double predictionAccuracy;
    
    /**
     * 数据更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 样本数量
     */
    private Integer sampleSize;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * 趋势指标（上升/下降/稳定）
     */
    private String trend;
    
    /**
     * 季节性因子
     */
    private Double seasonalFactor;
    
    /**
     * 工作日/周末因子
     */
    private Double dayTypeFactor;
}
