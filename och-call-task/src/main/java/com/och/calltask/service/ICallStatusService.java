package com.och.calltask.service;


import java.util.List;

/**
 * 呼叫状态管理服务接口
 * 
 * @author danmo
 * @date 2025/01/15
 */
public interface ICallStatusService {

    /**
     * 更新呼叫状态
     * 
     * @param callId 呼叫ID
     * @param status 状态
     * @param message 消息
     */
    void updateCallStatus(Long callId, String status, String message);

    /**
     * 获取呼叫状态
     * 
     * @param callId 呼叫ID
     * @return 呼叫状态信息
     */
    CallStatusInfo getCallStatus(Long callId);

    /**
     * 获取任务的所有呼叫状态
     * 
     * @param taskId 任务ID
     * @return 呼叫状态列表
     */
    List<CallStatusInfo> getTaskCallStatuses(Long taskId);

    /**
     * 获取坐席的所有呼叫状态
     * 
     * @param agentId 坐席ID
     * @return 呼叫状态列表
     */
    List<CallStatusInfo> getAgentCallStatuses(Long agentId);

    /**
     * 获取呼叫统计信息
     * 
     * @param taskId 任务ID
     * @return 统计信息
     */
    CallStatusStatistics getCallStatistics(Long taskId);

    /**
     * 清理过期的呼叫状态
     * 
     * @param hours 保留小时数
     */
    void cleanupExpiredCallStatuses(int hours);

    /**
     * 添加呼叫状态信息
     * 
     * @param statusInfo 呼叫状态信息
     */
    void addCallStatus(CallStatusInfo statusInfo);

    /**
     * 呼叫状态信息
     */
    class CallStatusInfo {
        private Long callId;
        private Long taskId;
        private Long agentId;
        private Long contactId;
        private String status;
        private String message;
        private Long startTime;
        private Long endTime;
        private Long duration;
        private String hangupCause;
        private String uniqueId;
        private String contactPhone;
        private String agentNumber;

        // getters and setters
        public Long getCallId() { return callId; }
        public void setCallId(Long callId) { this.callId = callId; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Long getAgentId() { return agentId; }
        public void setAgentId(Long agentId) { this.agentId = agentId; }
        public Long getContactId() { return contactId; }
        public void setContactId(Long contactId) { this.contactId = contactId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }
        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }
        public Long getDuration() { return duration; }
        public void setDuration(Long duration) { this.duration = duration; }
        public String getHangupCause() { return hangupCause; }
        public void setHangupCause(String hangupCause) { this.hangupCause = hangupCause; }
        public String getUniqueId() { return uniqueId; }
        public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getAgentNumber() { return agentNumber; }
        public void setAgentNumber(String agentNumber) { this.agentNumber = agentNumber; }
    }

    /**
     * 呼叫统计信息
     */
    class CallStatusStatistics {
        private Long taskId;
        private Integer totalCalls;
        private Integer answeredCalls;
        private Integer noAnswerCalls;
        private Integer busyCalls;
        private Integer failedCalls;
        private Double answerRate;
        private Long totalDuration;
        private Long averageDuration;
        private Integer activeCalls;

        // getters and setters
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Integer getTotalCalls() { return totalCalls; }
        public void setTotalCalls(Integer totalCalls) { this.totalCalls = totalCalls; }
        public Integer getAnsweredCalls() { return answeredCalls; }
        public void setAnsweredCalls(Integer answeredCalls) { this.answeredCalls = answeredCalls; }
        public Integer getNoAnswerCalls() { return noAnswerCalls; }
        public void setNoAnswerCalls(Integer noAnswerCalls) { this.noAnswerCalls = noAnswerCalls; }
        public Integer getBusyCalls() { return busyCalls; }
        public void setBusyCalls(Integer busyCalls) { this.busyCalls = busyCalls; }
        public Integer getFailedCalls() { return failedCalls; }
        public void setFailedCalls(Integer failedCalls) { this.failedCalls = failedCalls; }
        public Double getAnswerRate() { return answerRate; }
        public void setAnswerRate(Double answerRate) { this.answerRate = answerRate; }
        public Long getTotalDuration() { return totalDuration; }
        public void setTotalDuration(Long totalDuration) { this.totalDuration = totalDuration; }
        public Long getAverageDuration() { return averageDuration; }
        public void setAverageDuration(Long averageDuration) { this.averageDuration = averageDuration; }
        public Integer getActiveCalls() { return activeCalls; }
        public void setActiveCalls(Integer activeCalls) { this.activeCalls = activeCalls; }
    }
}
