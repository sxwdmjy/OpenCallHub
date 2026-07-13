package com.och.calltask.service.impl;

import com.och.acd.service.impl.QueueDispatchServiceImpl;
import com.och.calltask.application.algorithm.PredictiveAlgorithm;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundDialAttempt;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 预测拨号服务
 * <p>
 * 根据坐席空闲率和预测算法引擎，动态计算拨号数量，最大化坐席利用率。
 * 算法能力由 {@link PredictiveAlgorithm} 策略接口提供，支持不同算法实现的热切换。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictiveDialerServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.OutboundDialAttemptMapper, com.och.calltask.domain.OutboundDialAttempt> implements com.och.calltask.service.IPredictiveDialerService {

    private final QueueDispatchServiceImpl queueDispatchService;
    private final OutboundQueueServiceImpl queueService;
    private final PredictiveAlgorithm predictiveAlgorithm;

    /** 最大拨号数上限，防止呼叫过载 */
    private static final int MAX_DIAL_COUNT = 50;

    /**
     * 计算本轮预测拨号数
     * <p>
     * 综合考虑空闲坐席数量和算法预测的接通率，给出本轮最优拨号数。
     * </p>
     *
     * @param skillGroupId 技能组 ID
     * @return 建议的本轮拨号数
     */
    public double calculateDialRate(Long skillGroupId) {
        // 第1步：获取空闲坐席数
        QueueDispatchServiceImpl.QueueStats stats = queueDispatchService.getQueueStats(skillGroupId);
        int idleAgents = stats.availableAgents();

        if (idleAgents <= 0) {
            log.debug("[PredictiveDialer] No idle agents for skillGroup={}, dialRate=0", skillGroupId);
            return 0;
        }

        // 第2步：通过预测算法获取接通率
        double answerRate = predictiveAlgorithm.predictAnswerProbability(null);

        log.debug("[PredictiveDialer] skillGroup={}, idleAgents={}, answerRate={}",
                skillGroupId, idleAgents, String.format("%.2f%%", answerRate * 100));

        // 第3步：通过预测算法计算最优拨号数
        int optimalCount = predictiveAlgorithm.calculateOptimalDialCount(idleAgents, answerRate);

        // 限制上限，防止过载
        optimalCount = Math.min(optimalCount, MAX_DIAL_COUNT);

        log.info("[PredictiveDialer] skillGroup={}, idleAgents={}, answerRate={}, optimalCount={}",
                skillGroupId, idleAgents, String.format("%.2f%%", answerRate * 100), optimalCount);

        return optimalCount;
    }

    /**
     * 从队列中取出下一个客户并拨号
     * <p>结合算法预测和队列出队，实现预测式外呼的完整拨号循环：
     * 1. 计算当前最优拨号数 → 判断是否应该拨号
     * 2. 如果可以拨号，从队列中取出下一个 SCHEDULED 客户
     * 3. 创建拨号尝试记录
     * </p>
     *
     * @param skillGroupId 技能组ID
     * @param taskId       外呼任务ID
     * @return 拨号尝试记录（如果无可用客户则返回 null）
     */
    @Transactional
    public OutboundDialAttempt dialNext(Long skillGroupId, Long taskId) {
        double rate = calculateDialRate(skillGroupId);
        if (rate <= 0) {
            return null;
        }

        OutboundCustomer customer = queueService.dequeue(taskId);
        if (customer == null) {
            log.debug("[PredictiveDialer] Queue empty: taskId={}", taskId);
            return null;
        }

        int attemptNo = customer.getAttemptCount() != null ? customer.getAttemptCount() + 1 : 1;
        String dedupKey = taskId + ":" + customer.getId() + ":" + attemptNo;

        OutboundDialAttempt attempt = OutboundDialAttempt.builder()
                .taskId(taskId)
                .customerId(customer.getId())
                .attemptNo(attemptNo)
                .dedupKey(dedupKey)
                .state("DIALING")
                .dialTime(LocalDateTime.now())
                .build();
        baseMapper.insert(attempt);

        log.info("[PredictiveDialer] Dial initiated: taskId={}, customerId={}, phone={}, optimalCount={}",
                taskId, customer.getId(), customer.getPhoneNumber(), rate);
        return attempt;
    }

    /**
     * 记录拨号完成并更新预测模型
     * <p>
     * 当外呼系统回调拨号结果时调用此方法，将结果反馈给预测算法以持续优化。
     * </p>
     *
     * @param attempt 包含最终拨号结果的尝试记录
     */
    public void recordDialResult(OutboundDialAttempt attempt) {
        if (attempt == null || attempt.getDialResult() == null) {
            return;
        }
        predictiveAlgorithm.updateModel(attempt);
        log.debug("[PredictiveDialer] Model updated: dialResult={}, phone={}",
                attempt.getDialResult(), attempt.getCustomerId());
    }
}
