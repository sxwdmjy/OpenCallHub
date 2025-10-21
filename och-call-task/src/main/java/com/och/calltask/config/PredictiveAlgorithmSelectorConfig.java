package com.och.calltask.config;

import com.och.calltask.service.IPredictiveAlgorithmService;
import com.och.calltask.service.impl.PredictiveAlgorithmProductionServiceImpl;
import com.och.calltask.service.impl.PredictiveAlgorithmServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 预测算法选择配置类
 * 根据配置选择使用生产级算法还是基础算法
 */
@Configuration
public class PredictiveAlgorithmSelectorConfig {
    
    @Value("${predictive.algorithm.production.enabled:true}")
    private boolean useProductionAlgorithm;

    /**
     * 预测算法服务
     */
    @Bean
    @Primary
    public IPredictiveAlgorithmService predictiveAlgorithmService(
            PredictiveAlgorithmServiceImpl basicService,
            PredictiveAlgorithmProductionServiceImpl productionService) {
        
        if (useProductionAlgorithm) {
            return productionService;
        } else {
            return basicService;
        }
    }
}