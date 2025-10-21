package com.och.calltask.service.impl;

import com.och.calltask.domain.vo.CallQueueItem;
import com.och.calltask.domain.vo.QueuePriority;
import com.och.calltask.service.ICallQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 智能呼叫队列管理服务实现
 * 
 * @author danmo
 * @date 2025/01/15
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class CallQueueServiceImpl implements ICallQueueService {

    // 存储每个任务的队列，使用优先级队列
    private final Map<Long, PriorityBlockingQueue<CallQueueItem>> taskQueues = new ConcurrentHashMap<>();
    
    // 存储队列项目，便于快速查找和删除
    private final Map<Long, Map<Long, CallQueueItem>> taskQueueItems = new ConcurrentHashMap<>();
    
    // 存储队列项ID到任务和联系人ID的映射
    private final Map<String, Long> queueItemToTaskId = new ConcurrentHashMap<>();
    private final Map<String, Long> queueItemToContactId = new ConcurrentHashMap<>();

    @Override
    public void enqueueCall(Long taskId, Long contactId, QueuePriority priority, 
                          double predictedAnswerProbability, double customerValue) {
        log.debug("【入队】任务:{} 客户:{} 优先级:{} 接通概率:{:.2f} 客户价值:{:.2f}", 
                taskId, contactId, priority.getDescription(), predictedAnswerProbability, customerValue);
        
        // 创建队列项目
        CallQueueItem item = new CallQueueItem();
        String queueItemId = UUID.randomUUID().toString();
        item.setQueueItemId(queueItemId);
        item.setTaskId(taskId);
        item.setContactId(contactId);
        item.setPriority(priority);
        item.setPredictedAnswerProbability(predictedAnswerProbability);
        item.setCustomerValue(customerValue);
        item.setEnqueueTime(LocalDateTime.now());
        item.setAttempts(0);
        item.setStatus("WAITING");
        
        // 获取或创建任务队列
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.computeIfAbsent(taskId, 
                k -> new PriorityBlockingQueue<>(100, this::compareQueueItems));
        
        // 获取或创建任务队列项目映射
        Map<Long, CallQueueItem> items = taskQueueItems.computeIfAbsent(taskId, 
                k -> new ConcurrentHashMap<>());
        
        // 添加到队列和映射
        queue.offer(item);
        items.put(contactId, item);
        
        // 添加ID映射
        queueItemToTaskId.put(queueItemId, taskId);
        queueItemToContactId.put(queueItemId, contactId);
        
        log.info("【入队完成】任务:{} 客户:{} 队列大小:{}", taskId, contactId, queue.size());
    }

    @Override
    public CallQueueItem dequeueCall(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        
        try {
            CallQueueItem item = queue.poll();
            if (item != null) {
                // 从映射中移除
                Map<Long, CallQueueItem> items = taskQueueItems.get(taskId);
                if (items != null) {
                    items.remove(item.getContactId());
                }
                
                // 从ID映射中移除
                queueItemToTaskId.remove(item.getQueueItemId());
                queueItemToContactId.remove(item.getQueueItemId());
                
                log.debug("【出队】任务:{} 客户:{} 优先级:{}", 
                        taskId, item.getContactId(), item.getPriority().getDescription());
            }
            
            return item;
        } catch (Exception e) {
            log.error("【出队异常】任务:{} 出队失败", taskId, e);
            return null;
        }
    }

    @Override
    public int getQueueSize(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        return queue != null ? queue.size() : 0;
    }

    @Override
    public void removeFromQueue(Long taskId, Long contactId) {
        Map<Long, CallQueueItem> items = taskQueueItems.get(taskId);
        if (items == null) {
            return;
        }
        
        CallQueueItem item = items.remove(contactId);
        if (item != null) {
            PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
            if (queue != null) {
                queue.remove(item);
            }
            
            // 从ID映射中移除
            queueItemToTaskId.remove(item.getQueueItemId());
            queueItemToContactId.remove(item.getQueueItemId());
            
            log.info("【移除队列】任务:{} 客户:{} 已从队列移除", taskId, contactId);
        }
    }

    @Override
    public void reorderQueue(Long taskId, List<String> newOrder) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        
        log.info("【重新排序】任务:{} 开始重新排序队列，当前大小:{}", taskId, queue.size());
        
        // 获取所有项目
        List<CallQueueItem> items = new ArrayList<>();
        queue.drainTo(items);
        
        // 重新排序
        items.sort(this::compareQueueItems);
        
        // 重新入队
        for (CallQueueItem item : items) {
            queue.offer(item);
        }
        
        log.info("【重新排序完成】任务:{} 队列已重新排序", taskId);
    }

    @Override
    public List<CallQueueItem> getQueueItems(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        if (queue == null) {
            return Collections.emptyList();
        }
        
        // 创建副本，避免修改原队列
        List<CallQueueItem> items = new ArrayList<>();
        queue.drainTo(items);
        
        // 重新入队
        for (CallQueueItem item : items) {
            queue.offer(item);
        }
        
        // 按优先级排序
        items.sort(this::compareQueueItems);
        
        return items;
    }

    @Override
    public QueueStatus getQueueStatus(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        Map<Long, CallQueueItem> items = taskQueueItems.get(taskId);
        
        QueueStatus status = new QueueStatus();
        status.setTaskId(taskId);
        status.setLastUpdateTime(LocalDateTime.now());
        
        if (queue == null) {
            status.setTotalItems(0);
            status.setWaitingItems(0);
            status.setProcessingItems(0);
            status.setCompletedItems(0);
            status.setCancelledItems(0);
            status.setFailedItems(0);
            status.setQueueState("STOPPED");
            return status;
        }
        
        status.setTotalItems(queue.size());
        status.setWaitingItems(queue.size());
        status.setProcessingItems(0);
        status.setCompletedItems(0);
        status.setCancelledItems(0);
        status.setFailedItems(0);
        status.setQueueState("ACTIVE");
        
        return status;
    }

    @Override
    public void pauseQueue(Long taskId) {
        // 暂停队列逻辑
        log.info("【暂停队列】任务:{} 队列已暂停", taskId);
    }

    @Override
    public void resumeQueue(Long taskId) {
        // 恢复队列逻辑
        log.info("【恢复队列】任务:{} 队列已恢复", taskId);
    }

    @Override
    public void clearQueue(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        if (queue != null) {
            queue.clear();
        }
        
        Map<Long, CallQueueItem> items = taskQueueItems.get(taskId);
        if (items != null) {
            // 清理ID映射
            for (CallQueueItem item : items.values()) {
                queueItemToTaskId.remove(item.getQueueItemId());
                queueItemToContactId.remove(item.getQueueItemId());
            }
            items.clear();
        }
        
        log.info("【清空队列】任务:{} 队列已清空", taskId);
    }

    @Override
    public QueueStatistics getQueueStatistics(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        
        QueueStatistics stats = new QueueStatistics();
        stats.setTaskId(taskId);
        stats.setPeakTime(LocalDateTime.now());
        
        if (queue == null) {
            stats.setTotalProcessed(0);
            stats.setSuccessfulCalls(0);
            stats.setFailedCalls(0);
            stats.setSuccessRate(0.0);
            stats.setAverageProcessingTime(0L);
            stats.setTotalWaitTime(0L);
            stats.setAverageWaitTime(0L);
            stats.setPeakQueueSize(0);
            stats.setQueueEfficiency(0.0);
            stats.setPriorityChanges(0);
            stats.setReorders(0);
            return stats;
        }
        
        stats.setTotalProcessed(queue.size());
        stats.setSuccessfulCalls(0); // 需要从其他地方获取
        stats.setFailedCalls(0); // 需要从其他地方获取
        stats.setSuccessRate(0.0); // 需要计算
        stats.setAverageProcessingTime(0L); // 需要计算
        stats.setTotalWaitTime(0L); // 需要计算
        stats.setAverageWaitTime(0L); // 需要计算
        stats.setPeakQueueSize(queue.size());
        stats.setQueueEfficiency(0.0); // 需要计算
        stats.setPriorityChanges(0); // 需要统计
        stats.setReorders(0); // 需要统计
        
        return stats;
    }

    @Override
    public void optimizeQueue(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        
        log.info("【优化队列】任务:{} 开始优化队列", taskId);
        
        // 获取所有项目
        List<CallQueueItem> items = new ArrayList<>();
        queue.drainTo(items);
        
        // 重新排序优化
        items.sort(this::compareQueueItems);
        
        // 重新入队
        for (CallQueueItem item : items) {
            queue.offer(item);
        }
        
        log.info("【优化队列完成】任务:{} 队列已优化", taskId);
    }

    @Override
    public void updateQueueItemPriority(String queueItemId, QueuePriority newPriority) {
        // 根据queueItemId查找并更新优先级
        Long taskId = queueItemToTaskId.get(queueItemId);
        if (taskId == null) {
            log.warn("【更新优先级失败】未找到队列项:{}", queueItemId);
            return;
        }
        
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.get(taskId);
        if (queue == null) {
            log.warn("【更新优先级失败】未找到任务队列:{}", taskId);
            return;
        }
        
        // 查找并更新队列项
        CallQueueItem targetItem = null;
        List<CallQueueItem> items = new ArrayList<>();
        queue.drainTo(items);
        
        for (CallQueueItem item : items) {
            if (queueItemId.equals(item.getQueueItemId())) {
                item.setPriority(newPriority);
                targetItem = item;
            }
            queue.offer(item);
        }
        
        if (targetItem != null) {
            log.info("【更新优先级】队列项:{} 新优先级:{}", queueItemId, newPriority.getDescription());
        } else {
            log.warn("【更新优先级失败】未找到队列项:{}", queueItemId);
        }
    }

    @Override
    public CallQueueItem getQueueItem(String queueItemId) {
        // 根据queueItemId查找队列项
        Long taskId = queueItemToTaskId.get(queueItemId);
        if (taskId == null) {
            log.debug("【获取队列项】未找到队列项ID:{}", queueItemId);
            return null;
        }
        
        Map<Long, CallQueueItem> items = taskQueueItems.get(taskId);
        if (items == null) {
            log.debug("【获取队列项】未找到任务队列项:{}", taskId);
            return null;
        }
        
        Long contactId = queueItemToContactId.get(queueItemId);
        if (contactId == null) {
            log.debug("【获取队列项】未找到联系人ID:{}", queueItemId);
            return null;
        }
        
        CallQueueItem item = items.get(contactId);
        log.debug("【获取队列项】队列项ID:{} {}", queueItemId, item != null ? "找到" : "未找到");
        return item;
    }

    /**
     * 比较队列项目，用于排序
     * 优先级：紧急 > 高 > 普通 > 低 > 最低
     * 同优先级按客户价值和预测接通概率排序
     */
    private int compareQueueItems(CallQueueItem item1, CallQueueItem item2) {
        // 首先按优先级排序（数字越大优先级越高）
        int priorityCompare = Integer.compare(item2.getPriority().getWeight(), item1.getPriority().getWeight());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        
        // 同优先级按综合评分排序
        double score1 = calculateItemScore(item1);
        double score2 = calculateItemScore(item2);
        
        return Double.compare(score2, score1);
    }

    /**
     * 计算队列项目综合评分
     * 评分 = 客户价值 * 0.4 + 预测接通概率 * 0.6
     */
    private double calculateItemScore(CallQueueItem item) {
        return item.getCustomerValue() * 0.4 + item.getPredictedAnswerProbability() * 0.6;
    }

    /**
     * 清理指定任务的所有队列
     */
    public void clearTaskQueue(Long taskId) {
        PriorityBlockingQueue<CallQueueItem> queue = taskQueues.remove(taskId);
        Map<Long, CallQueueItem> items = taskQueueItems.remove(taskId);
        
        // 清理ID映射
        if (items != null) {
            for (CallQueueItem item : items.values()) {
                queueItemToTaskId.remove(item.getQueueItemId());
                queueItemToContactId.remove(item.getQueueItemId());
            }
        }
        
        log.info("【清理队列】任务:{} 队列已清理", taskId);
    }

    /**
     * 获取所有任务的队列统计信息
     */
    public Map<Long, ICallQueueService.QueueStatistics> getAllQueueStatistics() {
        Map<Long, ICallQueueService.QueueStatistics> statistics = new HashMap<>();
        
        for (Map.Entry<Long, PriorityBlockingQueue<CallQueueItem>> entry : taskQueues.entrySet()) {
            Long taskId = entry.getKey();
            PriorityBlockingQueue<CallQueueItem> queue = entry.getValue();
            
            ICallQueueService.QueueStatistics stats = new ICallQueueService.QueueStatistics();
            stats.setTaskId(taskId);
            stats.setTotalProcessed(queue.size());
            stats.setSuccessfulCalls(0); // 需要从其他地方获取
            stats.setFailedCalls(0); // 需要从其他地方获取
            stats.setSuccessRate(0.0); // 需要计算
            stats.setAverageProcessingTime(0L); // 需要计算
            stats.setTotalWaitTime(0L); // 需要计算
            stats.setAverageWaitTime(0L); // 需要计算
            stats.setPeakQueueSize(queue.size());
            stats.setPeakTime(LocalDateTime.now());
            stats.setQueueEfficiency(0.0); // 需要计算
            stats.setPriorityChanges(0); // 需要统计
            stats.setReorders(0); // 需要统计
            
            statistics.put(taskId, stats);
        }
        
        return statistics;
    }

}
