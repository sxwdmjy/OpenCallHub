package com.och.calltask.service;

import com.och.calltask.domain.vo.CallQueueItem;
import com.och.calltask.domain.vo.QueuePriority;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能呼叫队列管理服务接口
 * 
 * @author danmo
 * @date 2025/01/15
 */
public interface ICallQueueService {

    /**
     * 将客户添加到呼叫队列
     * 
     * @param taskId 任务ID
     * @param contactId 客户ID
     * @param priority 队列优先级
     * @param predictedAnswerProbability 预测接通概率
     * @param customerValue 客户价值评分
     */
    void enqueueCall(Long taskId, Long contactId, QueuePriority priority, double predictedAnswerProbability, double customerValue);

    /**
     * 从队列中获取下一个待拨打的客户
     * 
     * @param taskId 任务ID
     * @return 队列中的客户项，如果没有则返回null
     */
    CallQueueItem dequeueCall(Long taskId);

    /**
     * 获取指定任务的当前队列大小
     * 
     * @param taskId 任务ID
     * @return 队列大小
     */
    int getQueueSize(Long taskId);

    /**
     * 移除队列项
     * 
     * @param taskId 任务ID
     * @param contactId 客户ID
     */
    void removeFromQueue(Long taskId, Long contactId);

    /**
     * 获取队列状态
     * 
     * @param taskId 任务ID
     * @return 队列状态信息
     */
    QueueStatus getQueueStatus(Long taskId);

    /**
     * 重新排序队列
     * 
     * @param taskId 任务ID
     * @param newOrder 新的排序
     */
    void reorderQueue(Long taskId, List<String> newOrder);

    /**
     * 暂停队列
     * 
     * @param taskId 任务ID
     */
    void pauseQueue(Long taskId);

    /**
     * 恢复队列
     * 
     * @param taskId 任务ID
     */
    void resumeQueue(Long taskId);

    /**
     * 清空队列
     * 
     * @param taskId 任务ID
     */
    void clearQueue(Long taskId);

    /**
     * 获取队列统计信息
     * 
     * @param taskId 任务ID
     * @return 队列统计
     */
    QueueStatistics getQueueStatistics(Long taskId);

    /**
     * 优化队列顺序
     * 
     * @param taskId 任务ID
     */
    void optimizeQueue(Long taskId);

    /**
     * 获取队列中的呼叫列表
     * 
     * @param taskId 任务ID
     * @return 队列中的呼叫列表
     */
    List<CallQueueItem> getQueueItems(Long taskId);

    /**
     * 更新队列项优先级
     * 
     * @param queueItemId 队列项ID
     * @param newPriority 新优先级
     */
    void updateQueueItemPriority(String queueItemId, QueuePriority newPriority);

    /**
     * 获取队列项详情
     * 
     * @param queueItemId 队列项ID
     * @return 队列项详情
     */
    CallQueueItem getQueueItem(String queueItemId);

    /**
     * 队列项状态枚举
     */
    enum QueueItemStatus {
        WAITING,    // 等待中
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        CANCELLED,  // 已取消
        FAILED      // 失败
    }

    /**
     * 队列状态信息
     */
    class QueueStatus {
        private Long taskId;
        private Integer totalItems;
        private Integer waitingItems;
        private Integer processingItems;
        private Integer completedItems;
        private Integer cancelledItems;
        private Integer failedItems;
        private Long averageWaitTime;
        private Long maxWaitTime;
        private String queueState; // ACTIVE, PAUSED, STOPPED
        private LocalDateTime lastUpdateTime;

        // getters and setters
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Integer getTotalItems() { return totalItems; }
        public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
        public Integer getWaitingItems() { return waitingItems; }
        public void setWaitingItems(Integer waitingItems) { this.waitingItems = waitingItems; }
        public Integer getProcessingItems() { return processingItems; }
        public void setProcessingItems(Integer processingItems) { this.processingItems = processingItems; }
        public Integer getCompletedItems() { return completedItems; }
        public void setCompletedItems(Integer completedItems) { this.completedItems = completedItems; }
        public Integer getCancelledItems() { return cancelledItems; }
        public void setCancelledItems(Integer cancelledItems) { this.cancelledItems = cancelledItems; }
        public Integer getFailedItems() { return failedItems; }
        public void setFailedItems(Integer failedItems) { this.failedItems = failedItems; }
        public Long getAverageWaitTime() { return averageWaitTime; }
        public void setAverageWaitTime(Long averageWaitTime) { this.averageWaitTime = averageWaitTime; }
        public Long getMaxWaitTime() { return maxWaitTime; }
        public void setMaxWaitTime(Long maxWaitTime) { this.maxWaitTime = maxWaitTime; }
        public String getQueueState() { return queueState; }
        public void setQueueState(String queueState) { this.queueState = queueState; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }

    /**
     * 队列统计信息
     */
    class QueueStatistics {
        private Long taskId;
        private Integer totalProcessed;
        private Integer successfulCalls;
        private Integer failedCalls;
        private Double successRate;
        private Long averageProcessingTime;
        private Long totalWaitTime;
        private Long averageWaitTime;
        private Integer peakQueueSize;
        private LocalDateTime peakTime;
        private Double queueEfficiency;
        private Integer priorityChanges;
        private Integer reorders;

        // getters and setters
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Integer getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(Integer totalProcessed) { this.totalProcessed = totalProcessed; }
        public Integer getSuccessfulCalls() { return successfulCalls; }
        public void setSuccessfulCalls(Integer successfulCalls) { this.successfulCalls = successfulCalls; }
        public Integer getFailedCalls() { return failedCalls; }
        public void setFailedCalls(Integer failedCalls) { this.failedCalls = failedCalls; }
        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }
        public Long getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(Long averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
        public Long getTotalWaitTime() { return totalWaitTime; }
        public void setTotalWaitTime(Long totalWaitTime) { this.totalWaitTime = totalWaitTime; }
        public Long getAverageWaitTime() { return averageWaitTime; }
        public void setAverageWaitTime(Long averageWaitTime) { this.averageWaitTime = averageWaitTime; }
        public Integer getPeakQueueSize() { return peakQueueSize; }
        public void setPeakQueueSize(Integer peakQueueSize) { this.peakQueueSize = peakQueueSize; }
        public LocalDateTime getPeakTime() { return peakTime; }
        public void setPeakTime(LocalDateTime peakTime) { this.peakTime = peakTime; }
        public Double getQueueEfficiency() { return queueEfficiency; }
        public void setQueueEfficiency(Double queueEfficiency) { this.queueEfficiency = queueEfficiency; }
        public Integer getPriorityChanges() { return priorityChanges; }
        public void setPriorityChanges(Integer priorityChanges) { this.priorityChanges = priorityChanges; }
        public Integer getReorders() { return reorders; }
        public void setReorders(Integer reorders) { this.reorders = reorders; }
    }
}
