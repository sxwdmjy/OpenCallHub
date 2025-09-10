package com.och.calltask.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预测式外呼结果
 * 
 * @author danmo
 * @date 2025/01/15
 */
@Data
public class PredictiveDialingResult {
    
    /**
     * 建议拨号数量
     */
    private Integer recommendedDialCount;
    
    /**
     * 预测成功率
     */
    private Double predictedSuccessRate;
    
    /**
     * 预期坐席利用率
     */
    private Double expectedAgentUtilization;
    
    /**
     * 预期接通数量
     */
    private Integer expectedAnswerCount;
    
    /**
     * 建议拨号间隔（秒）
     */
    private Integer recommendedDialInterval;
    
    /**
     * 优先级客户列表
     */
    private List<Long> priorityContactIds;
    
    /**
     * 风险等级（低/中/高）
     */
    private String riskLevel;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * 预测时间
     */
    private LocalDateTime predictionTime;
    
    /**
     * 算法版本
     */
    private String algorithmVersion;
    
    /**
     * 预测说明
     */
    private String predictionNote;
    
    /**
     * 建议调整参数
     */
    private String adjustmentSuggestions;
    
    /**
     * 预期收益评分
     */
    private Double expectedRevenueScore;
    
    /**
     * 资源消耗预估
     */
    private Double resourceConsumptionEstimate;
}
