package com.och.acd.application.strategy;

import com.och.acd.domain.AcdAgentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 * <p>
 * 按固定顺序轮流分配坐席，每个技能组维护一个独立的轮询计数器。
 * 适用于所有坐席技能等级相同的场景。
 * </p>
 *
 * @author danmo
 */
@Component
public class RoundRobinStrategy implements DispatchStrategy {

    /** 每个技能组的轮询计数器（key: skillGroupId, value: 当前索引） */
    private final Map<Long, AtomicInteger> roundRobinIndex = new ConcurrentHashMap<>();

    @Override
    public AcdAgentStatus select(List<AcdAgentStatus> availableAgents) {
        if (availableAgents == null || availableAgents.isEmpty()) return null;

        Long skillGroupId = availableAgents.get(0).getSkillGroupId();
        AtomicInteger index = roundRobinIndex.computeIfAbsent(skillGroupId, k -> new AtomicInteger(0));

        int current = index.getAndUpdate(i -> (i + 1) % availableAgents.size());
        return availableAgents.get(current % availableAgents.size());
    }

    @Override
    public String getName() {
        return "ROUND_ROBIN";
    }
}