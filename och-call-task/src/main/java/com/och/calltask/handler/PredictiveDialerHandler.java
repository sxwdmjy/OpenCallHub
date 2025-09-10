package com.och.calltask.handler;

import com.alibaba.fastjson2.JSONArray;
import com.och.calltask.domain.entity.CallTask;
import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.query.CallTaskContactQuery;
import com.och.calltask.domain.vo.CallTaskContactVo;
import com.och.calltask.service.ICallTaskAssignmentService;
import com.och.calltask.service.ICallTaskService;
import com.och.calltask.service.IPredictiveAlgorithmService;
import com.och.calltask.service.IPredictiveDialingService;
import com.och.calltask.service.ICallQueueService;
import com.och.calltask.service.ICallStatusService;
import com.och.calltask.domain.vo.PredictiveDialingMetrics;
import com.och.calltask.domain.vo.PredictiveDialingResult;
import com.och.calltask.domain.vo.PredictiveDialingCallResult;
import com.och.calltask.domain.vo.CallQueueItem;
import com.och.calltask.domain.vo.QueuePriority;
import com.och.common.utils.StringUtils;
import com.och.system.domain.vo.agent.SipAgentStatusVo;
import com.och.system.domain.vo.agent.SipSimpleAgent;
import com.och.system.service.ISipAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 预测式任务处理
 * @author danmo
 * @date 2025/06/26
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class PredictiveDialerHandler implements CallTaskHandler {

    private final ICallTaskService callTaskService;
    private final ISipAgentService sipAgentService;
    private final ICallTaskAssignmentService callTaskAssignmentService;
    private final IPredictiveAlgorithmService predictiveAlgorithmService;
    private final IPredictiveDialingService predictiveDialingService;
    private final ICallQueueService callQueueService;
    private final ICallStatusService callStatusService;

    // 预测式外呼配置参数
    private static final int MAX_CONCURRENT_CALLS = 5; // 最大并发外呼数
    private static final double TARGET_AGENT_UTILIZATION = 0.85; // 目标坐席利用率
    private static final int DIAL_INTERVAL_SECONDS = 3; // 拨号间隔（秒）
    private static final double INITIAL_ANSWER_RATE = 0.25; // 初始预测接通率
    private static final double MIN_ANSWER_RATE = 0.15; // 最小预测接通率
    private static final double MAX_ANSWER_RATE = 0.45; // 最大预测接通率

    @Override
    public void execute(Long taskId) {
        log.info("【预测式外呼】开始处理任务ID: {}", taskId);
        
        try {
            // 1. 获取任务基础信息
            CallTask callTask = callTaskService.getById(taskId);
            if (callTask == null) {
                log.warn("【任务异常】任务不存在, ID: {}", taskId);
                return;
            }

            // 2. 获取可用坐席
            List<Long> agentIds = parseAgentIds(callTask.getAgentList());
            List<SipAgentStatusVo> availableAgents = getAvailableAgents(agentIds);
            
            if (CollectionUtils.isEmpty(availableAgents)) {
                log.warn("【无可用坐席】任务:{} 无可用坐席", taskId);
                return;
            }

            // 3. 使用高级预测算法计算最优拨号策略
            PredictiveDialingMetrics historicalMetrics = predictiveAlgorithmService.getTaskPredictionMetrics(taskId);
            PredictiveDialingResult predictionResult = predictiveAlgorithmService.calculateOptimalDialCount(
                    taskId, availableAgents.size(), historicalMetrics);
            
            log.info("【高级预测】任务:{} 建议拨号:{} 预期成功率:{:.2f}% 风险等级:{} 置信度:{:.2f}", 
                    taskId, predictionResult.getRecommendedDialCount(), 
                    predictionResult.getPredictedSuccessRate() * 100,
                    predictionResult.getRiskLevel(), predictionResult.getConfidence());

            // 4. 初始化呼叫队列
            initializeCallQueue(taskId, predictionResult);
            
            // 5. 执行智能预测式拨号（使用队列管理）
            executeAdvancedPredictiveDialingWithQueue(availableAgents, callTask, predictionResult);

        } catch (Exception e) {
            log.error("【预测式外呼异常】任务:{} 执行失败", taskId, e);
        }
    }

    /**
     * 获取可用坐席
     */
    private List<SipAgentStatusVo> getAvailableAgents(List<Long> agentIds) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }
        
        List<SipAgentStatusVo> agentStatusList = sipAgentService.getAgentStatusList(agentIds);
        
        // 过滤出空闲状态的坐席
        return agentStatusList.stream()
                .filter(agent -> agent.getOnlineStatus() == 1 && agent.getStatus() == 1) // 在线且空闲
                .toList();
    }

    /**
     * 计算预测拨号数量
     * 基于坐席数量、目标利用率和动态预测接通率计算
     */
    private int calculatePredictedDialCount(List<SipAgentStatusVo> availableAgents, CallTask callTask) {
        int availableAgentCount = availableAgents.size();
        
        // 获取动态预测接通率
        double predictedAnswerRate = getDynamicAnswerRate(callTask.getId());
        
        // 计算当前坐席利用率
        double currentUtilization = calculateCurrentAgentUtilization(callTask.getId(), availableAgents);
        
        // 动态调整目标利用率
        double adjustedTargetUtilization = adjustTargetUtilization(currentUtilization);
        
        // 基础计算：目标坐席数 * 调整后目标利用率 / 动态预测接通率
        double baseDialCount = availableAgentCount * adjustedTargetUtilization / predictedAnswerRate;
        
        // 应用任务配置的接待限制
        Integer receiveLimit = callTask.getReceiveLimit();
        if (receiveLimit != null && receiveLimit > 0) {
            baseDialCount = Math.min(baseDialCount, receiveLimit);
        }
        
        // 确保不超过最大并发数
        int maxCalls = Math.min(MAX_CONCURRENT_CALLS, availableAgentCount);
        
        int finalCount = Math.min((int) Math.ceil(baseDialCount), maxCalls);
        
        log.info("【预测计算详情】坐席数:{}, 当前利用率:{:.2f}, 调整后目标利用率:{:.2f}, 预测接通率:{:.2f}, 计算拨号数:{}", 
                availableAgentCount, currentUtilization, adjustedTargetUtilization, predictedAnswerRate, finalCount);
        
        return finalCount;
    }

    /**
     * 获取动态预测接通率
     * 基于历史数据动态调整预测接通率
     */
    private double getDynamicAnswerRate(Long taskId) {
        // 获取任务历史接通率数据
        double historicalAnswerRate = getHistoricalAnswerRate(taskId);
        
        // 如果历史数据不足，使用初始预测值
        if (historicalAnswerRate <= 0) {
            return INITIAL_ANSWER_RATE;
        }
        
        // 动态调整：结合历史数据和初始预测值
        double adjustedRate = historicalAnswerRate * 0.7 + INITIAL_ANSWER_RATE * 0.3;
        
        // 限制在合理范围内
        return Math.max(MIN_ANSWER_RATE, Math.min(MAX_ANSWER_RATE, adjustedRate));
    }

    /**
     * 获取历史接通率
     */
    private double getHistoricalAnswerRate(Long taskId) {
        try {
            // 查询任务历史接通数据
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(taskId);
            query.setStatus(1); // 已分配
            query.setCallStatus(1); // 已拨打
            
            List<CallTaskContactVo> dialedContacts = callTaskService.getTaskContactList(query);
            
            if (CollectionUtils.isEmpty(dialedContacts)) {
                return 0.0;
            }
            
            // 计算接通率（这里简化处理，实际应该查询通话记录）
            // 假设30%的已拨打客户被接通
            return 0.3;
            
        } catch (Exception e) {
            log.warn("【历史数据获取失败】任务:{} 获取历史接通率失败", taskId, e);
            return 0.0;
        }
    }

    /**
     * 计算当前坐席利用率
     */
    private double calculateCurrentAgentUtilization(Long taskId, List<SipAgentStatusVo> availableAgents) {
        try {
            // 查询当前正在处理的客户数量
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(taskId);
            query.setStatus(1); // 已分配
            query.setCallStatus(0); // 未拨打（正在处理中）
            
            List<CallTaskContactVo> processingContacts = callTaskService.getTaskContactList(query);
            int processingCount = CollectionUtils.isEmpty(processingContacts) ? 0 : processingContacts.size();
            
            int totalAgents = availableAgents.size();
            if (totalAgents == 0) {
                return 0.0;
            }
            
            return (double) processingCount / totalAgents;
            
        } catch (Exception e) {
            log.warn("【利用率计算失败】任务:{} 计算坐席利用率失败", taskId, e);
            return 0.0;
        }
    }

    /**
     * 动态调整目标利用率
     */
    private double adjustTargetUtilization(double currentUtilization) {
        if (currentUtilization < TARGET_AGENT_UTILIZATION * 0.5) {
            // 利用率过低，提高目标利用率
            return Math.min(TARGET_AGENT_UTILIZATION + 0.1, 0.95);
        } else if (currentUtilization > TARGET_AGENT_UTILIZATION * 1.2) {
            // 利用率过高，降低目标利用率
            return Math.max(TARGET_AGENT_UTILIZATION - 0.1, 0.6);
        }
        
        return TARGET_AGENT_UTILIZATION;
    }

    /**
     * 获取待拨号客户（带优先级）
     * 使用高级预测算法提供的优先级列表
     */
    private List<CallTaskContactVo> getContactsToDialWithPriority(Long taskId, int limit, List<Long> priorityContactIds) {
        CallTaskContactQuery query = new CallTaskContactQuery();
        query.setStatus(0); // 未分配状态
        query.setTaskId(taskId);
        query.setCallStatus(0); // 未拨打状态
        
        List<CallTaskContactVo> allContacts = callTaskService.getTaskContactList(query);
        
        if (CollectionUtils.isEmpty(allContacts)) {
            return Collections.emptyList();
        }
        
        if (allContacts.size() <= limit) {
            return allContacts;
        }
        
        // 如果有优先级列表，优先选择优先级客户
        if (CollectionUtils.isNotEmpty(priorityContactIds)) {
            return selectContactsByPriority(allContacts, priorityContactIds, limit);
        }
        
        // 否则使用智能选择策略
        return selectOptimalContacts(allContacts, limit);
    }

    /**
     * 根据优先级选择客户
     */
    private List<CallTaskContactVo> selectContactsByPriority(List<CallTaskContactVo> allContacts, 
                                                           List<Long> priorityContactIds, 
                                                           int limit) {
        List<CallTaskContactVo> selectedContacts = new ArrayList<>();
        
        // 首先选择优先级客户
        for (Long priorityId : priorityContactIds) {
            if (selectedContacts.size() >= limit) {
                break;
            }
            
            allContacts.stream()
                    .filter(contact -> contact.getId().equals(priorityId))
                    .findFirst()
                    .ifPresent(selectedContacts::add);
        }
        
        // 如果优先级客户不足，补充其他客户
        if (selectedContacts.size() < limit) {
            List<CallTaskContactVo> remainingContacts = allContacts.stream()
                    .filter(contact -> !selectedContacts.contains(contact))
                    .toList();
            
            int needMore = limit - selectedContacts.size();
            selectedContacts.addAll(remainingContacts.subList(0, Math.min(needMore, remainingContacts.size())));
        }
        
        log.info("【优先级选择】从{}个客户中选择了{}个优先级客户", allContacts.size(), selectedContacts.size());
        return selectedContacts;
    }

    /**
     * 获取待拨号客户
     * 使用智能选择策略，优先选择高价值客户
     */
    private List<CallTaskContactVo> getContactsToDial(Long taskId, int limit) {
        CallTaskContactQuery query = new CallTaskContactQuery();
        query.setStatus(0); // 未分配状态
        query.setTaskId(taskId);
        query.setCallStatus(0); // 未拨打状态
        
        List<CallTaskContactVo> allContacts = callTaskService.getTaskContactList(query);
        
        if (CollectionUtils.isEmpty(allContacts)) {
            return Collections.emptyList();
        }
        
        if (allContacts.size() <= limit) {
            return allContacts;
        }
        
        // 智能客户选择策略
        return selectOptimalContacts(allContacts, limit);
    }

    /**
     * 智能客户选择策略
     * 基于客户价值、历史接通率等因素进行选择
     */
    private List<CallTaskContactVo> selectOptimalContacts(List<CallTaskContactVo> allContacts, int limit) {
        // 按优先级排序：优先选择高价值客户
        List<CallTaskContactVo> sortedContacts = allContacts.stream()
                .sorted((c1, c2) -> {
                    // 这里可以根据实际业务需求添加客户价值评分逻辑
                    // 例如：根据客户等级、历史购买记录、地区等因素评分
                    return compareContactValue(c1, c2);
                })
                .toList();
        
        // 从高价值客户中选择，但保持一定的随机性
        List<CallTaskContactVo> selectedContacts = new ArrayList<>();
        int selectedCount = 0;
        
        for (CallTaskContactVo contact : sortedContacts) {
            if (selectedCount >= limit) {
                break;
            }
            
            // 80%概率选择高价值客户，20%概率随机选择
            if (ThreadLocalRandom.current().nextDouble() < 0.8 || selectedCount < limit * 0.5) {
                selectedContacts.add(contact);
                selectedCount++;
            }
        }
        
        // 如果选择数量不足，随机补充
        if (selectedCount < limit) {
            List<CallTaskContactVo> remainingContacts = allContacts.stream()
                    .filter(contact -> !selectedContacts.contains(contact))
                    .toList();
            
            Collections.shuffle(remainingContacts);
            int needMore = limit - selectedCount;
            selectedContacts.addAll(remainingContacts.subList(0, Math.min(needMore, remainingContacts.size())));
        }
        
        log.info("【客户选择】从{}个客户中智能选择了{}个客户", allContacts.size(), selectedContacts.size());
        return selectedContacts;
    }

    /**
     * 比较客户价值
     * 这里可以根据实际业务需求实现客户价值评分
     */
    private int compareContactValue(CallTaskContactVo c1, CallTaskContactVo c2) {
        // 简化实现：按姓名排序（实际应该根据客户价值评分）
        return c1.getName() != null && c2.getName() != null ? 
                c1.getName().compareTo(c2.getName()) : 0;
    }

    /**
     * 初始化呼叫队列
     * 将客户添加到智能队列中，按优先级和预测概率排序
     */
    private void initializeCallQueue(Long taskId, PredictiveDialingResult predictionResult) {
        log.info("【初始化队列】任务:{} 开始初始化呼叫队列", taskId);
        
        try {
            // 获取待拨号客户
            List<CallTaskContactVo> contactsToDial = getContactsToDialWithPriority(
                    taskId, predictionResult.getRecommendedDialCount(), predictionResult.getPriorityContactIds());
            
            if (CollectionUtils.isEmpty(contactsToDial)) {
                log.info("【无待拨号】任务:{} 无待拨号客户", taskId);
                return;
            }
            
            // 将客户添加到队列
            for (CallTaskContactVo contact : contactsToDial) {
                // 预测客户接通概率
                double answerProbability = predictiveAlgorithmService.predictContactAnswerProbability(
                        contact.getId(), taskId);
                
                // 计算客户价值评分（简化实现）
                double customerValue = calculateCustomerValue(contact);
                
                // 确定优先级
                QueuePriority priority = determineContactPriority(contact, answerProbability, customerValue);
                
                // 添加到队列
                callQueueService.enqueueCall(taskId, contact.getId(), priority, 
                        answerProbability, customerValue);
            }
            
            log.info("【队列初始化完成】任务:{} 已添加{}个客户到队列", taskId, contactsToDial.size());
            
        } catch (Exception e) {
            log.error("【队列初始化异常】任务:{} 初始化失败", taskId, e);
        }
    }

    /**
     * 使用队列管理执行高级预测式拨号
     */
    private void executeAdvancedPredictiveDialingWithQueue(List<SipAgentStatusVo> agents, 
                                                         CallTask callTask,
                                                         PredictiveDialingResult predictionResult) {
        
        List<CallTaskAssignment> assignments = new ArrayList<>();
        int agentIndex = 0;
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        
        log.info("【队列拨号】任务:{} 开始从队列拨号，可用坐席:{}", 
                callTask.getId(), agents.size());
        
        // 持续从队列中获取客户进行拨号
        while (agentIndex < agents.size() && callQueueService.getQueueSize(callTask.getId()) > 0) {
            try {
                // 从队列获取下一个客户
                CallQueueItem queueItem = callQueueService.dequeueCall(callTask.getId());
                if (queueItem == null) {
                    break;
                }
                
                // 获取客户信息
                CallTaskContactVo contact = getContactById(queueItem.getContactId());
                if (contact == null) {
                    log.warn("【客户不存在】客户ID:{} 不存在", queueItem.getContactId());
                    failureCount++;
                    continue;
                }
                
                // 选择坐席
                SipAgentStatusVo selectedAgent = selectOptimalAgent(agents, agentIndex, callTask);
                if (selectedAgent == null) {
                    log.warn("【无可用坐席】客户:{} 无可用坐席", contact.getId());
                    // 将客户重新放回队列
                    callQueueService.enqueueCall(callTask.getId(), contact.getId(), 
                            queueItem.getPriority(), queueItem.getPredictedAnswerProbability(), 
                            queueItem.getCustomerValue());
                    failureCount++;
                    continue;
                }
                
                // 检查坐席分配限制
                if (!checkAgentAssignmentLimit(selectedAgent, callTask)) {
                    log.debug("【坐席限制】坐席:{} 已达到分配限制", selectedAgent.getId());
                    // 将客户重新放回队列
                    callQueueService.enqueueCall(callTask.getId(), contact.getId(), 
                            queueItem.getPriority(), queueItem.getPredictedAnswerProbability(), 
                            queueItem.getCustomerValue());
                    failureCount++;
                    continue;
                }
                
                // 创建分配记录
                CallTaskAssignment assignment = createAssignment(contact, selectedAgent);
                assignments.add(assignment);
                successCount++;
                
                // 执行拨号
                try {
                    PredictiveDialingCallResult dialResult = 
                            predictiveDialingService.executePredictiveCall(assignment);
                    
                    // 记录呼叫状态
                    recordCallStatus(callTask.getId(), dialResult.getCallId(), 
                            selectedAgent.getId(), contact.getId(), "INITIATED", "呼叫已发起");
                    
                    log.info("【拨号结果】客户:{} 呼叫ID:{} 状态:{} 消息:{}", 
                            contact.getId(), dialResult.getCallId(), dialResult.getStatus(), dialResult.getMessage());
                    
                } catch (Exception e) {
                    log.error("【拨号执行异常】客户:{} 拨号失败", contact.getId(), e);
                    failureCount++;
                    continue;
                }
                
                // 记录详细统计
                recordAdvancedDialingStats(callTask.getId(), contact.getId(), selectedAgent.getId(), 
                        queueItem.getPredictedAnswerProbability(), predictionResult.getConfidence());
                
                agentIndex++;
                
                // 动态调整拨号间隔
                if (agentIndex > 0) {
                    int dynamicInterval = predictionResult.getRecommendedDialInterval() != null ? 
                            predictionResult.getRecommendedDialInterval() : DIAL_INTERVAL_SECONDS;
                    Thread.sleep(dynamicInterval * 1000);
                }
                
            } catch (Exception e) {
                log.error("【队列拨号异常】客户拨号失败", e);
                failureCount++;
            }
        }
        
        // 批量保存分配记录
        if (!assignments.isEmpty()) {
            callTaskAssignmentService.updateBatchById(assignments);
        }
        
        // 高级性能统计
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double actualSuccessRate = (successCount + failureCount) > 0 ? 
                (double) successCount / (successCount + failureCount) : 0.0;
        double predictionAccuracy = calculatePredictionAccuracy(
                predictionResult.getPredictedSuccessRate(), actualSuccessRate);
        
        log.info("【队列拨号完成】任务:{} 成功:{} 失败:{} 实际成功率:{:.2f}% 预测准确率:{:.2f}% 耗时:{}ms 剩余队列:{}", 
                callTask.getId(), successCount, failureCount, actualSuccessRate * 100, 
                predictionAccuracy * 100, duration, callQueueService.getQueueSize(callTask.getId()));
        
        // 更新预测模型
        updatePredictionModelWithResults(callTask.getId(), successCount, failureCount, 
                actualSuccessRate, predictionResult);
    }

    /**
     * 执行高级预测式拨号
     * 使用AI预测算法和智能优化策略
     */
    private void executeAdvancedPredictiveDialing(List<CallTaskContactVo> contacts, 
                                                List<SipAgentStatusVo> agents, 
                                                CallTask callTask,
                                                PredictiveDialingResult predictionResult) {
        
        List<CallTaskAssignment> assignments = new ArrayList<>();
        int agentIndex = 0;
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        
        log.info("【高级预测拨号】任务:{} 准备拨号 {} 个客户，预期成功率:{:.2f}%", 
                callTask.getId(), contacts.size(), predictionResult.getPredictedSuccessRate() * 100);
        
        for (CallTaskContactVo contact : contacts) {
            try {
                // 预测客户接通概率
                double answerProbability = predictiveAlgorithmService.predictContactAnswerProbability(
                        contact.getId(), callTask.getId());
                
                // 根据预测概率调整拨号策略
                if (answerProbability < 0.2) {
                    log.debug("【低概率客户】客户:{} 接通概率:{:.2f}% 跳过", 
                            contact.getId(), answerProbability * 100);
                    continue;
                }
                
                // 动态调整拨号间隔（基于预测结果）
                if (agentIndex > 0) {
                    int dynamicInterval = predictionResult.getRecommendedDialInterval() != null ? 
                            predictionResult.getRecommendedDialInterval() : DIAL_INTERVAL_SECONDS;
                    Thread.sleep(dynamicInterval * 1000);
                }
                
                // 选择最优坐席
                SipAgentStatusVo selectedAgent = selectOptimalAgent(agents, agentIndex, callTask);
                if (selectedAgent == null) {
                    log.warn("【无可用坐席】客户:{} 无可用坐席", contact.getId());
                    failureCount++;
                    continue;
                }
                
                // 检查坐席分配限制
                if (!checkAgentAssignmentLimit(selectedAgent, callTask)) {
                    log.debug("【坐席限制】坐席:{} 已达到分配限制", selectedAgent.getId());
                    failureCount++;
                    continue;
                }
                
                // 创建分配记录
                CallTaskAssignment assignment = createAssignment(contact, selectedAgent);
                assignments.add(assignment);
                successCount++;
                
                // 执行真正的拨号
                try {
                    PredictiveDialingCallResult dialResult = 
                            predictiveDialingService.executePredictiveCall(assignment);
                    
                    log.info("【拨号结果】客户:{} 呼叫ID:{} 状态:{} 消息:{}", 
                            contact.getId(), dialResult.getCallId(), dialResult.getStatus(), dialResult.getMessage());
                    
                } catch (Exception e) {
                    log.error("【拨号执行异常】客户:{} 拨号失败", contact.getId(), e);
                    failureCount++;
                    continue;
                }
                
                // 记录详细统计
                recordAdvancedDialingStats(callTask.getId(), contact.getId(), selectedAgent.getId(), 
                        answerProbability, predictionResult.getConfidence());
                
                agentIndex++;
                
            } catch (Exception e) {
                log.error("【高级拨号异常】客户:{} 拨号失败", contact.getId(), e);
                failureCount++;
            }
        }
        
        // 批量保存分配记录
        if (!assignments.isEmpty()) {
            callTaskAssignmentService.updateBatchById(assignments);
        }
        
        // 高级性能统计
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double actualSuccessRate = contacts.size() > 0 ? (double) successCount / contacts.size() : 0.0;
        double predictionAccuracy = calculatePredictionAccuracy(
                predictionResult.getPredictedSuccessRate(), actualSuccessRate);
        
        log.info("【高级拨号完成】任务:{} 成功:{} 失败:{} 实际成功率:{:.2f}% 预测准确率:{:.2f}% 耗时:{}ms", 
                callTask.getId(), successCount, failureCount, actualSuccessRate * 100, 
                predictionAccuracy * 100, duration);
        
        // 更新预测模型
        updatePredictionModelWithResults(callTask.getId(), successCount, failureCount, 
                actualSuccessRate, predictionResult);
    }

    /**
     * 执行预测式拨号
     * 包含性能监控和自适应调整
     */
    private void executePredictiveDialing(List<CallTaskContactVo> contacts, 
                                        List<SipAgentStatusVo> agents, 
                                        CallTask callTask) {
        
        List<CallTaskAssignment> assignments = new ArrayList<>();
        int agentIndex = 0;
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        
        log.info("【开始拨号】任务:{} 准备拨号 {} 个客户，可用坐席 {} 个", 
                callTask.getId(), contacts.size(), agents.size());
        
        for (CallTaskContactVo contact : contacts) {
            try {
                // 动态调整拨号间隔
                if (agentIndex > 0) {
                    int dynamicInterval = calculateDynamicDialInterval(callTask.getId(), successCount, failureCount);
                    Thread.sleep(dynamicInterval * 1000);
                }
                
                // 选择最优坐席
                SipAgentStatusVo selectedAgent = selectOptimalAgent(agents, agentIndex, callTask);
                if (selectedAgent == null) {
                    log.warn("【无可用坐席】客户:{} 无可用坐席", contact.getId());
                    failureCount++;
                    continue;
                }
                
                // 检查坐席分配限制
                if (!checkAgentAssignmentLimit(selectedAgent, callTask)) {
                    log.debug("【坐席限制】坐席:{} 已达到分配限制", selectedAgent.getId());
                    failureCount++;
                    continue;
                }
                
                // 创建分配记录
                CallTaskAssignment assignment = createAssignment(contact, selectedAgent);
                assignments.add(assignment);
                successCount++;
                
                // 记录拨号统计
                recordDialingStats(callTask.getId(), contact.getId(), selectedAgent.getId());
                
                agentIndex++;
                
            } catch (Exception e) {
                log.error("【拨号异常】客户:{} 拨号失败", contact.getId(), e);
                failureCount++;
            }
        }
        
        // 批量保存分配记录
        if (!assignments.isEmpty()) {
            callTaskAssignmentService.updateBatchById(assignments);
        }
        
        // 性能统计和日志
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double successRate = !contacts.isEmpty() ? (double) successCount / contacts.size() : 0.0;
        
        log.info("【拨号完成】任务:{} 成功:{} 失败:{} 成功率:{}% 耗时:{}ms",
                callTask.getId(), successCount, failureCount, successRate * 100, duration);
        
        // 更新任务性能指标
        updateTaskPerformanceMetrics(callTask.getId(), successCount, failureCount, duration);
    }

    /**
     * 选择最优坐席
     * 基于坐席负载、技能匹配等因素选择
     */
    private SipAgentStatusVo selectOptimalAgent(List<SipAgentStatusVo> agents, int currentIndex, CallTask callTask) {
        if (CollectionUtils.isEmpty(agents)) {
            return null;
        }
        
        // 轮询选择，但考虑坐席负载
        SipAgentStatusVo selectedAgent = agents.get(currentIndex % agents.size());
        
        // 检查坐席是否过载
        if (isAgentOverloaded(selectedAgent, callTask)) {
            // 寻找负载较轻的坐席
            selectedAgent = agents.stream()
                    .filter(agent -> !isAgentOverloaded(agent, callTask))
                    .findFirst()
                    .orElse(agents.get(currentIndex % agents.size()));
        }
        
        return selectedAgent;
    }

    /**
     * 检查坐席是否过载
     */
    private boolean isAgentOverloaded(SipAgentStatusVo agent, CallTask callTask) {
        // 简化实现：检查坐席当前分配数量
        try {
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setTaskId(callTask.getId());
            query.setAgentIds(Collections.singletonList(agent.getId()));
            query.setStatus(1); // 已分配
            query.setCallStatus(0); // 未拨打
            
            List<CallTaskContactVo> currentAssignments = callTaskService.getTaskContactList(query);
            int currentCount = CollectionUtils.isEmpty(currentAssignments) ? 0 : currentAssignments.size();
            
            // 如果当前分配数超过限制的80%，认为过载
            return currentCount >= callTask.getReceiveLimit() * 0.8;
            
        } catch (Exception e) {
            log.warn("【过载检查失败】坐席:{} 检查过载状态失败", agent.getId(), e);
            return false;
        }
    }

    /**
     * 计算动态拨号间隔
     * 根据成功率动态调整拨号间隔
     */
    private int calculateDynamicDialInterval(Long taskId, int successCount, int failureCount) {
        int totalAttempts = successCount + failureCount;
        if (totalAttempts == 0) {
            return DIAL_INTERVAL_SECONDS;
        }
        
        double successRate = (double) successCount / totalAttempts;
        
        // 根据成功率调整间隔
        if (successRate > 0.8) {
            // 成功率高，可以加快拨号
            return Math.max(1, DIAL_INTERVAL_SECONDS - 1);
        } else if (successRate < 0.3) {
            // 成功率低，减慢拨号
            return DIAL_INTERVAL_SECONDS + 2;
        }
        
        return DIAL_INTERVAL_SECONDS;
    }

    /**
     * 记录拨号统计
     */
    private void recordDialingStats(Long taskId, Long contactId, Long agentId) {
        // 这里可以记录详细的拨号统计信息
        // 例如：拨号时间、坐席ID、客户ID等
        log.debug("【拨号记录】任务:{} 客户:{} 坐席:{}", taskId, contactId, agentId);
    }

    /**
     * 更新任务性能指标
     */
    private void updateTaskPerformanceMetrics(Long taskId, int successCount, int failureCount, long duration) {
        // 这里可以更新任务的性能指标
        // 例如：总拨号数、成功率、平均拨号时间等
        log.debug("【性能指标】任务:{} 成功:{} 失败:{} 耗时:{}ms", taskId, successCount, failureCount, duration);
    }

    /**
     * 记录高级拨号统计
     */
    private void recordAdvancedDialingStats(Long taskId, Long contactId, Long agentId, 
                                          double answerProbability, Double confidence) {
        log.debug("【高级拨号记录】任务:{} 客户:{} 坐席:{} 接通概率:{:.2f}% 置信度:{:.2f}", 
                taskId, contactId, agentId, answerProbability * 100, 
                confidence != null ? confidence * 100 : 0.0);
    }

    /**
     * 计算预测准确率
     */
    private double calculatePredictionAccuracy(double predictedRate, double actualRate) {
        if (predictedRate == 0) {
            return actualRate == 0 ? 1.0 : 0.0;
        }
        
        double accuracy = 1.0 - Math.abs(predictedRate - actualRate) / predictedRate;
        return Math.max(0.0, Math.min(1.0, accuracy));
    }

    /**
     * 使用结果更新预测模型
     */
    private void updatePredictionModelWithResults(Long taskId, int successCount, int failureCount, 
                                                double actualSuccessRate, PredictiveDialingResult predictionResult) {
        try {
            // 创建实际结果指标
            PredictiveDialingMetrics actualResults = new PredictiveDialingMetrics();
            actualResults.setTaskId(taskId);
            actualResults.setHistoricalAnswerRate(actualSuccessRate);
            actualResults.setSampleSize(successCount + failureCount);
            actualResults.setUpdateTime(java.time.LocalDateTime.now());
            
            // 更新预测模型
            predictiveAlgorithmService.updatePredictionModel(taskId, actualResults);
            
            log.info("【模型更新】任务:{} 实际成功率:{:.2f}% 预测成功率:{:.2f}% 模型已更新", 
                    taskId, actualSuccessRate * 100, 
                    predictionResult.getPredictedSuccessRate() * 100);
                    
        } catch (Exception e) {
            log.warn("【模型更新异常】任务:{} 更新失败", taskId, e);
        }
    }

    /**
     * 检查坐席分配限制
     */
    private boolean checkAgentAssignmentLimit(SipAgentStatusVo agent, CallTask callTask) {
        // 无限制直接通过
        if (Objects.equals(1, callTask.getIsPriority())) {
            return true;
        }
        
        // 检查坐席当前分配数量
        CallTaskContactQuery query = new CallTaskContactQuery();
        query.setStatus(1);
        query.setTaskId(callTask.getId());
        query.setAgentIds(Collections.singletonList(agent.getId()));
        query.setCallStatus(0);
        
        List<CallTaskContactVo> currentAssignments = callTaskService.getTaskContactList(query);
        int currentCount = CollectionUtils.isEmpty(currentAssignments) ? 0 : currentAssignments.size();
        
        return currentCount < callTask.getReceiveLimit();
    }

    /**
     * 创建分配记录
     */
    private CallTaskAssignment createAssignment(CallTaskContactVo contact, SipAgentStatusVo agent) {
        CallTaskAssignment assignment = new CallTaskAssignment();
        assignment.setId(contact.getId());
        assignment.setAgentId(agent.getId());
        assignment.setStatus(1); // 已分配
        assignment.setAssignmentTime(new Date());
        assignment.setCallStatus(0); // 未拨打
        assignment.setAttemptCount(0);
        return assignment;
    }

    /**
     * 解析坐席ID列表
     */
    private List<Long> parseAgentIds(String agentListJson) {
        if (!StringUtils.isNotBlank(agentListJson)) {
            return Collections.emptyList();
        }
        
        return JSONArray.parseArray(agentListJson, SipSimpleAgent.class)
                .stream()
                .map(SipSimpleAgent::getAgentId)
                .toList();
    }

    /**
     * 计算客户价值评分
     */
    private double calculateCustomerValue(CallTaskContactVo contact) {
        // 简化实现，实际应该根据客户等级、历史记录等因素计算
        double value = 0.5; // 基础值
        
        // 根据客户姓名长度模拟价值（实际应该根据业务规则）
        if (contact.getName() != null) {
            value += contact.getName().length() * 0.01;
        }
        
        // 根据电话号码模拟价值
        if (contact.getPhone() != null) {
            value += contact.getPhone().length() * 0.005;
        }
        
        return Math.min(1.0, Math.max(0.0, value));
    }

    /**
     * 确定客户优先级
     */
    private QueuePriority determineContactPriority(CallTaskContactVo contact, 
                                                 double answerProbability, 
                                                 double customerValue) {
        // 综合评分 = 客户价值 * 0.4 + 接通概率 * 0.6
        double score = customerValue * 0.4 + answerProbability * 0.6;
        
        if (score >= 0.8) {
            return QueuePriority.URGENT;
        } else if (score >= 0.7) {
            return QueuePriority.HIGH;
        } else if (score >= 0.5) {
            return QueuePriority.NORMAL;
        } else if (score >= 0.3) {
            return QueuePriority.LOW;
        } else {
            return QueuePriority.LOWEST;
        }
    }

    /**
     * 根据ID获取客户信息
     */
    private CallTaskContactVo getContactById(Long contactId) {
        try {
            // 这里应该根据contactId查询客户信息
            // 简化实现，返回模拟数据
            CallTaskContactVo contact = new CallTaskContactVo();
            contact.setId(contactId);
            contact.setPhone("13800138000");
            contact.setName("测试客户" + contactId);
            return contact;
        } catch (Exception e) {
            log.error("【获取客户信息异常】客户ID:{} 获取失败", contactId, e);
            return null;
        }
    }

    /**
     * 记录呼叫状态
     */
    private void recordCallStatus(Long taskId, Long callId, Long agentId, 
                                Long contactId, String status, String message) {
        try {
            ICallStatusService.CallStatusInfo statusInfo = new ICallStatusService.CallStatusInfo();
            statusInfo.setCallId(callId);
            statusInfo.setTaskId(taskId);
            statusInfo.setAgentId(agentId);
            statusInfo.setContactId(contactId);
            statusInfo.setStatus(status);
            statusInfo.setMessage(message);
            statusInfo.setStartTime(System.currentTimeMillis());
            
            callStatusService.addCallStatus(statusInfo);
            
        } catch (Exception e) {
            log.error("【记录呼叫状态异常】呼叫ID:{} 记录失败", callId, e);
        }
    }

    /**
     * 获取任务呼叫统计信息
     */
    public ICallStatusService.CallStatusStatistics getTaskCallStatistics(Long taskId) {
        return callStatusService.getCallStatistics(taskId);
    }

    /**
     * 获取队列统计信息
     */
    public Map<String, Object> getQueueStatistics(Long taskId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("queueSize", callQueueService.getQueueSize(taskId));
        stats.put("queueItems", callQueueService.getQueueItems(taskId));
        return stats;
    }
}
