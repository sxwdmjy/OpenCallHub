package com.och.calltask.handler;

import com.alibaba.fastjson2.JSONObject;
import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.query.CallTaskContactQuery;
import com.och.calltask.domain.vo.*;
import com.och.calltask.service.*;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.query.agent.SipAgentQuery;
import com.och.system.domain.vo.agent.SipAgentStatusVo;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.domain.vo.agent.SipSimpleAgent;
import com.och.system.service.ICallSkillService;
import com.och.system.service.ISipAgentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 预测式任务处理
 *
 * @author danmo
 * @date 2025/06/26
 */
@Slf4j
@Component
public class PredictiveDialerHandler implements CallTaskHandler {

    private final ICallTaskService callTaskService;
    private final ISipAgentService sipAgentService;
    private final ICallTaskAssignmentService callTaskAssignmentService;
    private final IPredictiveDialingService predictiveDialingService;
    private final ICallQueueService callQueueService;
    private final ICallSkillService callSkillService;
    private IPredictiveAlgorithmService predictiveAlgorithmService;

    //拨号间隔（秒）
    private static final int DIAL_INTERVAL_SECONDS = 3;
    //最大拨号次数

    public PredictiveDialerHandler(ICallTaskService callTaskService,
                                   ISipAgentService sipAgentService,
                                   ICallTaskAssignmentService callTaskAssignmentService,
                                   IPredictiveDialingService predictiveDialingService,
                                   ICallQueueService callQueueService,
                                   ICallSkillService callSkillService) {
        this.callTaskService = callTaskService;
        this.sipAgentService = sipAgentService;
        this.callTaskAssignmentService = callTaskAssignmentService;
        this.predictiveDialingService = predictiveDialingService;
        this.callQueueService = callQueueService;
        this.callSkillService = callSkillService;
    }
    
    public void setPredictiveAlgorithmService(IPredictiveAlgorithmService predictiveAlgorithmService) {
        this.predictiveAlgorithmService = predictiveAlgorithmService;
    }

    @Override
    public void execute(Long taskId) {
        log.info("【预测式外呼】开始处理任务ID: {}", taskId);

        try {
            // 1. 获取任务基础信息
            CallTaskVo callTask = callTaskService.getDetail(taskId);
            if (callTask == null) {
                log.warn("【任务异常】任务不存在, ID: {}", taskId);
                return;
            }
            
            // 根据任务类型执行不同的处理逻辑
            if (Objects.equals(0, callTask.getType())) {
                // 预测式外呼任务
                executeSkillGroupCall(callTask);
            } else if (Objects.equals(1, callTask.getType())) {
                // 其他类型任务，这里可以扩展
                log.info("【任务类型】暂不支持的任务类型: {}, 任务ID: {}", callTask.getType(), taskId);
            }

        } catch (Exception e) {
            log.error("【预测式外呼异常】任务:{} 执行失败", taskId, e);
        }
    }

    /**
     * 执行技能组任务
     * @param callTask 任务
     */
    private void executeSkillGroupCall(CallTaskVo callTask){
        Long taskId = callTask.getId();
        // 2. 获取可用坐席
        List<Long> agentIds = callSkillService.getAgentListBySkillId(Long.valueOf(callTask.getTransferValue()));
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
        executeAgentPredictiveDialingWithQueue(availableAgents, callTask, predictionResult);
    }

    /**
     * 获取可用坐席
     */
    private List<SipAgentStatusVo> getAvailableAgents(List<Long> agentIds) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }

        List<SipAgentStatusVo> agentStatusList = sipAgentService.getAgentStatusList(agentIds);
        // 过滤出空闲状态的坐席 在线且空闲
        return agentStatusList.stream()
                .filter(agent -> agent.getOnlineStatus() == 1 && agent.getStatus() == 1)
                .toList();
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
     * 智能客户选择策略
     * 基于客户价值、历史接通率等因素进行选择
     */
    private List<CallTaskContactVo> selectOptimalContacts(List<CallTaskContactVo> allContacts, int limit) {
        // 按优先级排序：优先选择高价值客户
        // 这里可以根据实际业务需求添加客户价值评分逻辑
        // 例如：根据客户等级、历史购买记录、地区等因素评分
        List<CallTaskContactVo> sortedContacts = allContacts.stream()
                .sorted(this::compareContactValue)
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
            List<CallTaskContactVo> remainingContacts = new ArrayList<>(allContacts.stream()
                    .filter(contact -> !selectedContacts.contains(contact))
                    .toList());

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
     * 使用队列管理执行坐席预测式拨号
     */
    private void executeAgentPredictiveDialingWithQueue(List<SipAgentStatusVo> agents,
                                                           CallTaskVo callTask,
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
                    predictiveDialingService.executePredictiveCall(assignment, callTask);

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
                int dynamicInterval = predictionResult.getRecommendedDialInterval() != null ?
                        predictionResult.getRecommendedDialInterval() : DIAL_INTERVAL_SECONDS;
                Thread.sleep(dynamicInterval * 1000L);

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
     * 选择最优坐席
     * 基于坐席负载、技能匹配等因素选择
     */
    private SipAgentStatusVo selectOptimalAgent(List<SipAgentStatusVo> agents, int currentIndex, CallTaskVo callTask) {
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
    private boolean isAgentOverloaded(SipAgentStatusVo agent, CallTaskVo callTask) {
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
     * 记录高级拨号统计
     */
    private void recordAdvancedDialingStats(Long taskId, Long contactId, Long agentId,
                                            double answerProbability, Double confidence) {
        log.debug("【高级拨号记录】任务:{} 客户:{} 坐席:{} 接通概率:{:.2f}% 置信度:{:.2f}%",
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
    private boolean checkAgentAssignmentLimit(SipAgentStatusVo agent, CallTaskVo callTask) {
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
    private List<Long> parseAgentIds(List<SipSimpleAgent> agentList) {
        if (CollectionUtils.isEmpty(agentList)) {
            SipAgentQuery query = new SipAgentQuery();
            query.setStatus(0);
            query.setOnlineStatus(0);
            List<SipAgentVo> sipAgentList = sipAgentService.getInfoByQuery(query);
            if (CollectionUtils.isEmpty(sipAgentList)) {
                return Collections.emptyList();
            }
            return sipAgentList.parallelStream().map(SipAgentVo::getId).toList();
        }
        return agentList.parallelStream().map(SipSimpleAgent::getAgentId).toList();
    }

    /**
     * 计算客户价值评分
     */
    private double calculateCustomerValue(CallTaskContactVo contact) {
        // 更加完善的客户价值评分计算
        double value = 0.5; // 基础值

        // 根据客户来源计算价值
        if (contact.getSource() != null) {
            switch (contact.getSource()) {
                case 0: // 人群导入
                    value += 0.2;
                    break;
                case 1: // 文件导入
                    value += 0.1;
                    break;
                case 2: // API导入
                    value += 0.3;
                    break;
                default:
                    value += 0.1;
                    break;
            }
        }

        // 根据拨打次数计算价值（拨打次数越少价值越高）
        if (contact.getAttemptCount() != null) {
            if (contact.getAttemptCount() == 0) {
                value += 0.3;
            } else if (contact.getAttemptCount() == 1) {
                value += 0.2;
            } else if (contact.getAttemptCount() == 2) {
                value += 0.1;
            }
        }

        // 根据客户扩展信息计算价值
        if (contact.getExt() != null && !contact.getExt().isEmpty()) {
            value += 0.1 * contact.getExt().size(); // 每个扩展字段增加0.1价值
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
            // 修复：直接从任务联系人表获取客户信息，而不是从分配表
            CallTaskContactQuery query = new CallTaskContactQuery();
            query.setId(contactId);
            List<CallTaskContactVo> contacts = callTaskService.getTaskContactList(query);
            if (CollectionUtils.isEmpty(contacts)) {
                log.info("【未获取客户信息】客户ID:{} 获取失败", contactId);
                return null;
            }
            return contacts.get(0);
        } catch (Exception e) {
            log.error("【获取客户信息异常】客户ID:{} 获取失败", contactId, e);
            return null;
        }
    }

}