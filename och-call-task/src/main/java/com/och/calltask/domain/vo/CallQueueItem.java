package com.och.calltask.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 呼叫队列项
 * 
 * @author danmo
 * @date 2025/01/15
 */
@Data
public class CallQueueItem {
    
    /**
     * 队列项ID
     */
    private String queueItemId;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 客户ID
     */
    private Long contactId;
    
    /**
     * 坐席ID
     */
    private Long agentId;
    
    /**
     * 客户姓名
     */
    private String contactName;
    
    /**
     * 客户电话
     */
    private String contactPhone;
    
    /**
     * 优先级
     */
    private QueuePriority priority;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 开始处理时间
     */
    private LocalDateTime startTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
    
    /**
     * 等待时间（秒）
     */
    private Long waitTime;
    
    /**
     * 处理时间（秒）
     */
    private Long processingTime;
    
    /**
     * 预测接通概率
     */
    private Double predictedAnswerProbability;
    
    /**
     * 客户价值评分
     */
    private Double customerValue;
    
    /**
     * 进入队列时间
     */
    private LocalDateTime enqueueTime;
    
    /**
     * 已尝试拨打次数
     */
    private Integer attempts;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 扩展数据
     */
    private String extData;
    
    /**
     * 队列位置
     */
    private Integer queuePosition;
    
    /**
     * 预计处理时间
     */
    private LocalDateTime estimatedProcessTime;
    
    /**
     * 实际处理时间
     */
    private LocalDateTime actualProcessTime;
    
    /**
     * 优先级调整历史
     */
    private String priorityChangeHistory;
    
    /**
     * 坐席技能匹配度
     */
    private Double agentSkillMatch;
    
    /**
     * 客户满意度预测
     */
    private Double predictedSatisfaction;
}
