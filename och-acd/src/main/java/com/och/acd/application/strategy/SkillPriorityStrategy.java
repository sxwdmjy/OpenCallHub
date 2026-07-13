package com.och.acd.application.strategy;

import com.och.acd.domain.AcdAgentStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 技能等级优先策略
 * <p>
 * 优先分配技能等级最高且空闲时间最长的坐席。
 * 适用于坐席技能等级差异较大的场景（如初级/中级/高级坐席）。
 * </p>
 *
 * @author danmo
 */
@Component
public class SkillPriorityStrategy implements DispatchStrategy {

    @Override
    public AcdAgentStatus select(List<AcdAgentStatus> availableAgents) {
        if (availableAgents == null || availableAgents.isEmpty()) return null;

        return availableAgents.stream()
                .max(Comparator.comparingInt(a -> a.getSkillLevel() != null ? a.getSkillLevel() : 0))
                .orElse(null);
    }

    @Override
    public String getName() {
        return "SKILL_PRIORITY";
    }
}