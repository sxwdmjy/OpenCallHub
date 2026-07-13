package com.och.calltask.application.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.DialResult;
import com.och.calltask.domain.OutboundDialAttempt;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 简单滚动平均算法
 * <p>
 * 基于滑动窗口统计最近 100 次拨号的接通率，作为预测依据。
 * 核心逻辑：
 * <ul>
 *   <li>接通率 = 窗口内 CONNECTED 数量 / 窗口大小</li>
 *   <li>最优拨号数 = idleAgents + (idleAgents * answerRate * 1.2)</li>
 *   <li>每次拨号结果通过 {@link #updateModel(OutboundDialAttempt)} 更新窗口</li>
 * </ul>
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class SimpleRollingAverageAlgorithm implements PredictiveAlgorithm {

    private final OutboundDialAttemptMapper dialAttemptMapper;

    /** 滑动窗口最大容量 */
    private static final int WINDOW_SIZE = 100;

    /** 无历史数据时的默认接通率 */
    private static final double DEFAULT_ANSWER_RATE = 0.3;

    /** 拨号倍率，拨号数略高于理论值以补偿未接通 */
    private static final double DIAL_MULTIPLIER = 1.2;

    /** 滑动窗口：存储每次拨号是否接通（true = CONNECTED） */
    private final Deque<Boolean> window = new ConcurrentLinkedDeque<>();

    /** 窗口内接通计数 */
    private volatile int connectedCount = 0;

    @PostConstruct
    public void init() {
        log.info("[SimpleRollingAverage] Initializing sliding window from DB, windowSize={}", WINDOW_SIZE);
        try {
            LambdaQueryWrapper<OutboundDialAttempt> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(OutboundDialAttempt::getDialResult)
                    .orderByDesc(OutboundDialAttempt::getDialTime)
                    .last("LIMIT " + WINDOW_SIZE);
            List<OutboundDialAttempt> recentAttempts = dialAttemptMapper.selectList(wrapper);

            // DB 返回的是时间倒序，需反转以保持时间升序
            for (int i = recentAttempts.size() - 1; i >= 0; i--) {
                addToWindow(recentAttempts.get(i));
            }
            log.info("[SimpleRollingAverage] Window initialized: size={}, connected={}, answerRate={}",
                    window.size(), connectedCount, String.format("%.2f%%", getCurrentAnswerRate() * 100));
        } catch (Exception e) {
            log.warn("[SimpleRollingAverage] Failed to init window from DB, starting empty", e);
        }
    }

    @Override
    public double predictAnswerProbability(String phoneNumber) {
        double rate = getCurrentAnswerRate();
        log.debug("[SimpleRollingAverage] predictAnswerProbability phone={}, rate={}", phoneNumber, rate);
        return rate;
    }

    @Override
    public int calculateOptimalDialCount(int idleAgents, double answerRate) {
        if (idleAgents <= 0) {
            return 0;
        }
        // 公式：idleAgents + (idleAgents * answerRate * 1.2)
        int count = idleAgents + (int) Math.round(idleAgents * answerRate * DIAL_MULTIPLIER);
        // 至少有 1 个拨号
        count = Math.max(1, count);
        log.debug("[SimpleRollingAverage] calculateOptimalDialCount idleAgents={}, answerRate={}, optimalCount={}",
                idleAgents, String.format("%.2f%%", answerRate * 100), count);
        return count;
    }

    @Override
    public void updateModel(OutboundDialAttempt attempt) {
        if (attempt == null || attempt.getDialResult() == null) {
            return;
        }
        addToWindow(attempt);
        log.debug("[SimpleRollingAverage] updateModel dialResult={}, windowSize={}, answerRate={}",
                attempt.getDialResult(), window.size(),
                String.format("%.2f%%", getCurrentAnswerRate() * 100));
    }

    // ======================== 内部方法 ========================

    /**
     * 获取当前滑动窗口的接通率
     */
    private double getCurrentAnswerRate() {
        int size = window.size();
        if (size == 0) {
            return DEFAULT_ANSWER_RATE;
        }
        return (double) connectedCount / size;
    }

    /**
     * 向滑动窗口添加一条拨号结果
     */
    private synchronized void addToWindow(OutboundDialAttempt attempt) {
        boolean isConnected = DialResult.CONNECTED.name().equals(attempt.getDialResult());
        window.addLast(isConnected);
        if (isConnected) {
            connectedCount++;
        }

        // 超出窗口容量时移除最早的数据
        while (window.size() > WINDOW_SIZE) {
            Boolean removed = window.removeFirst();
            if (removed != null && removed) {
                connectedCount--;
            }
        }
    }
}
