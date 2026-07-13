package com.och.acd.application.strategy;

import com.och.acd.domain.AcdAgentStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 最少通话数策略
 * <p>
 * 选择当前通话数最少的空闲坐席，确保工作负载按实际接听量均衡。
 * 当多个坐席通话数相同时，选择空闲时间更长的。
 * </p>
 *
 * @author danmo
 */
@Component
public class LeastCallsStrategy implements DispatchStrategy {

    @Override
    public AcdAgentStatus select(List<AcdAgentStatus> availableAgents) {
        if (availableAgents == null || availableAgents.isEmpty()) return null;

        return availableAgents.stream()
                .min(Comparator.comparingInt(a -> a.getTotalAnswered() != null ? a.getTotalAnswered() : 0))
                .orElse(null);
    }

    @Override
    public String getName() {
        return "LEAST_CALLS";
    }
}