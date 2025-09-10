package com.och.calltask.service.impl;

import com.och.calltask.service.ICallStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 呼叫状态管理服务实现
 * 
 * @author danmo
 * @date 2025/01/15
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class CallStatusServiceImpl implements ICallStatusService {

    // 存储呼叫状态信息
    private final Map<Long, CallStatusInfo> callStatuses = new ConcurrentHashMap<>();
    
    // 按任务ID索引的呼叫状态
    private final Map<Long, Set<Long>> taskCallIds = new ConcurrentHashMap<>();
    
    // 按坐席ID索引的呼叫状态
    private final Map<Long, Set<Long>> agentCallIds = new ConcurrentHashMap<>();

    @Override
    public void updateCallStatus(Long callId, String status, String message) {
        log.debug("【状态更新】呼叫ID:{} 状态:{} 消息:{}", callId, status, message);
        
        CallStatusInfo statusInfo = callStatuses.get(callId);
        if (statusInfo == null) {
            log.warn("【状态更新异常】呼叫ID:{} 状态信息不存在", callId);
            return;
        }
        
        // 更新状态
        statusInfo.setStatus(status);
        statusInfo.setMessage(message);
        
        // 如果是结束状态，更新结束时间
        if (isEndStatus(status)) {
            statusInfo.setEndTime(System.currentTimeMillis());
            if (statusInfo.getStartTime() != null) {
                statusInfo.setDuration(statusInfo.getEndTime() - statusInfo.getStartTime());
            }
        }
        
        log.info("【状态更新完成】呼叫ID:{} 状态:{} 消息:{}", callId, status, message);
    }

    @Override
    public CallStatusInfo getCallStatus(Long callId) {
        return callStatuses.get(callId);
    }

    @Override
    public List<CallStatusInfo> getTaskCallStatuses(Long taskId) {
        Set<Long> callIds = taskCallIds.get(taskId);
        if (callIds == null || callIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return callIds.stream()
                .map(callStatuses::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<CallStatusInfo> getAgentCallStatuses(Long agentId) {
        Set<Long> callIds = agentCallIds.get(agentId);
        if (callIds == null || callIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return callIds.stream()
                .map(callStatuses::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public CallStatusStatistics getCallStatistics(Long taskId) {
        List<CallStatusInfo> taskCalls = getTaskCallStatuses(taskId);
        
        CallStatusStatistics statistics = new CallStatusStatistics();
        statistics.setTaskId(taskId);
        statistics.setTotalCalls(taskCalls.size());
        
        // 统计各种状态
        int answeredCalls = 0;
        int noAnswerCalls = 0;
        int busyCalls = 0;
        int failedCalls = 0;
        int activeCalls = 0;
        long totalDuration = 0;
        
        for (CallStatusInfo call : taskCalls) {
            String status = call.getStatus();
            
            switch (status) {
                case "ANSWERED":
                    answeredCalls++;
                    break;
                case "NO_ANSWER":
                    noAnswerCalls++;
                    break;
                case "BUSY":
                    busyCalls++;
                    break;
                case "FAILED":
                case "TIMEOUT":
                case "CANCELLED":
                    failedCalls++;
                    break;
                case "DIALING":
                case "RINGING":
                case "PROGRESS":
                    activeCalls++;
                    break;
            }
            
            if (call.getDuration() != null) {
                totalDuration += call.getDuration();
            }
        }
        
        statistics.setAnsweredCalls(answeredCalls);
        statistics.setNoAnswerCalls(noAnswerCalls);
        statistics.setBusyCalls(busyCalls);
        statistics.setFailedCalls(failedCalls);
        statistics.setActiveCalls(activeCalls);
        
        // 计算接通率
        if (statistics.getTotalCalls() > 0) {
            statistics.setAnswerRate((double) answeredCalls / statistics.getTotalCalls());
        } else {
            statistics.setAnswerRate(0.0);
        }
        
        // 计算平均通话时长
        statistics.setTotalDuration(totalDuration);
        if (answeredCalls > 0) {
            statistics.setAverageDuration(totalDuration / answeredCalls);
        } else {
            statistics.setAverageDuration(0L);
        }
        
        return statistics;
    }

    @Override
    public void cleanupExpiredCallStatuses(int hours) {
        long expireTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000L);
        
        List<Long> expiredCallIds = callStatuses.entrySet().stream()
                .filter(entry -> {
                    CallStatusInfo statusInfo = entry.getValue();
                    return statusInfo.getStartTime() != null && statusInfo.getStartTime() < expireTime;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        for (Long callId : expiredCallIds) {
            removeCallStatus(callId);
        }
        
        log.info("【清理过期状态】清理了{}个过期的呼叫状态", expiredCallIds.size());
    }

    /**
     * 添加呼叫状态信息
     */
    public void addCallStatus(CallStatusInfo statusInfo) {
        Long callId = statusInfo.getCallId();
        callStatuses.put(callId, statusInfo);
        
        // 添加到任务索引
        if (statusInfo.getTaskId() != null) {
            taskCallIds.computeIfAbsent(statusInfo.getTaskId(), k -> new HashSet<>()).add(callId);
        }
        
        // 添加到坐席索引
        if (statusInfo.getAgentId() != null) {
            agentCallIds.computeIfAbsent(statusInfo.getAgentId(), k -> new HashSet<>()).add(callId);
        }
        
        log.debug("【添加状态】呼叫ID:{} 任务:{} 坐席:{}", 
                callId, statusInfo.getTaskId(), statusInfo.getAgentId());
    }

    /**
     * 移除呼叫状态信息
     */
    public void removeCallStatus(Long callId) {
        CallStatusInfo statusInfo = callStatuses.remove(callId);
        if (statusInfo != null) {
            // 从任务索引中移除
            if (statusInfo.getTaskId() != null) {
                Set<Long> taskCalls = taskCallIds.get(statusInfo.getTaskId());
                if (taskCalls != null) {
                    taskCalls.remove(callId);
                    if (taskCalls.isEmpty()) {
                        taskCallIds.remove(statusInfo.getTaskId());
                    }
                }
            }
            
            // 从坐席索引中移除
            if (statusInfo.getAgentId() != null) {
                Set<Long> agentCalls = agentCallIds.get(statusInfo.getAgentId());
                if (agentCalls != null) {
                    agentCalls.remove(callId);
                    if (agentCalls.isEmpty()) {
                        agentCallIds.remove(statusInfo.getAgentId());
                    }
                }
            }
            
            log.debug("【移除状态】呼叫ID:{} 已移除", callId);
        }
    }

    /**
     * 判断是否为结束状态
     */
    private boolean isEndStatus(String status) {
        return "ANSWERED".equals(status) || "NO_ANSWER".equals(status) || 
               "BUSY".equals(status) || "FAILED".equals(status) || 
               "TIMEOUT".equals(status) || "CANCELLED".equals(status);
    }

    /**
     * 获取所有呼叫状态统计
     */
    public Map<String, Object> getAllStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总呼叫数
        stats.put("totalCalls", callStatuses.size());
        
        // 按状态统计
        Map<String, Long> statusCounts = callStatuses.values().stream()
                .collect(Collectors.groupingBy(CallStatusInfo::getStatus, Collectors.counting()));
        stats.put("statusCounts", statusCounts);
        
        // 活跃呼叫数
        long activeCalls = callStatuses.values().stream()
                .mapToLong(call -> {
                    String status = call.getStatus();
                    return ("DIALING".equals(status) || "RINGING".equals(status) || "PROGRESS".equals(status)) ? 1 : 0;
                })
                .sum();
        stats.put("activeCalls", activeCalls);
        
        // 任务数
        stats.put("taskCount", taskCallIds.size());
        
        // 坐席数
        stats.put("agentCount", agentCallIds.size());
        
        return stats;
    }
}
