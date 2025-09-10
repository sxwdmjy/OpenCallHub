package com.och.calltask.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测式外呼呼叫结果
 * 
 * @author danmo
 * @date 2025/01/15
 */
@Data
public class PredictiveDialingCallResult {
    
    /**
     * 呼叫ID
     */
    private Long callId;
    
    /**
     * 任务分配ID
     */
    private Long assignmentId;
    
    /**
     * 呼叫状态
     */
    private String status;
    
    /**
     * 状态消息
     */
    private String message;
    
    /**
     * 呼叫开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 呼叫结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 呼叫时长（秒）
     */
    private Long duration;
    
    /**
     * 是否接通
     */
    private Boolean answered;
    
    /**
     * 挂机原因
     */
    private String hangupCause;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 录音文件路径
     */
    private String recordPath;
    
    /**
     * 坐席ID
     */
    private Long agentId;
    
    /**
     * 客户ID
     */
    private Long contactId;
    
    /**
     * 客户电话
     */
    private String contactPhone;
    
    /**
     * 外显号码
     */
    private String callerDisplay;
    
    /**
     * 通道ID
     */
    private String uniqueId;
    
    /**
     * 预测概率
     */
    private Double predictedProbability;
    
    /**
     * 实际结果
     */
    private String actualResult;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}