package com.och.acd.application.strategy;

import com.och.acd.domain.AcdAgentStatus;

import java.util.List;

/**
 * ACD 坐席分配策略接口（策略模式）
 * <p>
 * 定义从可用坐席列表中选择一个坐席的算法契约。
 * 不同策略实现不同的分配优先级：空闲时长、技能等级、轮询、最少通话数等。
 * </p>
 *
 * @author danmo
 */
public interface DispatchStrategy {

    /**
     * 从空闲坐席列表中选择一个坐席
     *
     * @param availableAgents 当前可接听的坐席列表（按某种规则预排序）
     * @return 选中的坐席，如果无可分配坐席则返回 null
     */
    AcdAgentStatus select(List<AcdAgentStatus> availableAgents);

    /**
     * 获取策略名称
     *
     * @return 策略标识名
     */
    String getName();
}