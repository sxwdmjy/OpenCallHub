package com.och.acd.application.strategy;

import com.och.acd.domain.AcdAgentStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 空闲优先策略（默认）
 * <p>
 * 选择空闲时间最长的坐席接听，确保工作负载均衡。
 * 调用方需传入按 lastStateTime 升序排列的坐席列表，本策略取第一个。
 * </p>
 *
 * @author danmo
 */
@Component
public class IdleFirstStrategy implements DispatchStrategy {

    @Override
    public AcdAgentStatus select(List<AcdAgentStatus> availableAgents) {
        return (availableAgents != null && !availableAgents.isEmpty()) ? availableAgents.get(0) : null;
    }

    @Override
    public String getName() {
        return "IDLE_FIRST";
    }
}