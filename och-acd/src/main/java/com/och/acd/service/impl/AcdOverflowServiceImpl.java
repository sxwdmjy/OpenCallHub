package com.och.acd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.acd.domain.AcdQueueSession;
import com.och.acd.domain.SkillGroup;
import com.och.acd.mapper.AcdQueueSessionMapper;
import com.och.acd.mapper.SkillGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ACD 溢出处理服务
 * <p>
 * 当队列等待人数超过技能组最大容量、或单个呼叫等待时间超过最大等待时间时，
 * 触发溢出机制：将超时呼叫改为 OVERFLOW 状态，记录溢出原因。
 * </p>
 *
 * <h3>溢出策略</h3>
 * <ul>
 *   <li>队列满 —— 新呼叫直接标记 OVERFLOW，不进入排队</li>
 *   <li>超时 —— 定时扫描 WAITING 状态的呼叫，超过 maxWaitSeconds 的标记 OVERFLOW</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcdOverflowServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.acd.mapper.AcdQueueSessionMapper, com.och.acd.domain.AcdQueueSession> implements com.och.acd.service.IAcdOverflowService {

    private final SkillGroupMapper skillGroupMapper;

    /**
     * 检查队列是否已满（是否允许新呼叫入队）
     *
     * @param skillGroupId 技能组ID
     * @return true 表示队列已满，不允许入队
     */
    public boolean isQueueFull(Long skillGroupId) {
        SkillGroup skillGroup = skillGroupMapper.selectById(skillGroupId);
        if (skillGroup == null || skillGroup.getMaxQueueSize() == null) {
            return false;
        }

        long currentCount = baseMapper.selectCount(
                new LambdaQueryWrapper<AcdQueueSession>()
                        .eq(AcdQueueSession::getSkillGroupId, skillGroupId)
                        .eq(AcdQueueSession::getState, "WAITING"));

        boolean isFull = currentCount >= skillGroup.getMaxQueueSize();
        if (isFull) {
            log.warn("[ACD溢出] 队列已满: skillGroup={}, maxSize={}, current={}",
                    skillGroupId, skillGroup.getMaxQueueSize(), currentCount);
        }
        return isFull;
    }

    /**
     * 扫描超时呼叫并标记溢出
     * <p>
     * 遍历 WAITING 状态的呼叫，将等待时间超过 maxWaitSeconds 的标记为 OVERFLOW。
     * 通常由定时任务（如每分钟）调用。
     * </p>
     *
     * @param skillGroupId 技能组ID
     * @return 本批次溢出的呼叫数
     */
    public int processTimeoutOverflow(Long skillGroupId) {
        SkillGroup skillGroup = skillGroupMapper.selectById(skillGroupId);
        if (skillGroup == null || skillGroup.getMaxWaitSeconds() == null) {
            return 0;
        }

        List<AcdQueueSession> waitingList = baseMapper.selectList(
                new LambdaQueryWrapper<AcdQueueSession>()
                        .eq(AcdQueueSession::getSkillGroupId, skillGroupId)
                        .eq(AcdQueueSession::getState, "WAITING"));

        int overflowCount = 0;
        LocalDateTime now = LocalDateTime.now();
        int maxWaitSeconds = skillGroup.getMaxWaitSeconds();

        for (AcdQueueSession session : waitingList) {
            long waitSeconds = Duration.between(session.getEnterTime(), now).getSeconds();
            if (waitSeconds > maxWaitSeconds) {
                session.setState("OVERFLOW");
                session.setEndTime(now);
                session.setEndCause("TIMEOUT");
                session.setWaitSeconds((int) waitSeconds);
                baseMapper.updateById(session);
                overflowCount++;
                log.info("[ACD溢出] 超时溢出: callId={}, waitSeconds={}, maxWaitSeconds={}",
                        session.getCallId(), waitSeconds, maxWaitSeconds);
            }
        }

        return overflowCount;
    }
}