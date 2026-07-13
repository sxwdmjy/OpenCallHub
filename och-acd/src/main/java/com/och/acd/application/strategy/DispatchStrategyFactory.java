package com.och.acd.application.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分配策略工厂（工厂模式）
 * <p>
 * 管理所有分配策略实现，根据策略名称返回对应策略。
 * 新增策略只需实现 DispatchStrategy 接口并注册为 Spring Bean，工厂自动发现。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class DispatchStrategyFactory {

    private final Map<String, DispatchStrategy> strategyMap;

    /**
     * Spring 自动注入所有 DispatchStrategy 实现，按 getName() 建立索引
     */
    public DispatchStrategyFactory(List<DispatchStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(DispatchStrategy::getName, Function.identity()));
        log.info("[DispatchStrategyFactory] 已加载策略: {}", strategyMap.keySet());
    }

    /**
     * 根据名称获取策略，默认返回空闲优先策略
     *
     * @param name 策略名称
     * @return 对应的策略实现
     */
    public DispatchStrategy getStrategy(String name) {
        DispatchStrategy strategy = strategyMap.get(name);
        if (strategy == null) {
            log.warn("[DispatchStrategyFactory] 未知策略: {}，使用默认 IDLE_FIRST", name);
            return strategyMap.get("IDLE_FIRST");
        }
        return strategy;
    }
}