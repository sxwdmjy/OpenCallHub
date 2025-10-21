package com.och.calltask.config;

import com.och.calltask.service.ICallTaskService;
import com.och.calltask.service.IPredictiveAlgorithmService;
import com.och.calltask.service.impl.PredictiveAlgorithmProductionServiceImpl;
import com.och.calltask.service.impl.PredictiveAlgorithmServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 预测算法配置类
 */
@Configuration
public class PredictiveAlgorithmConfig {
    
    @Autowired
    private ICallTaskService callTaskService;

    /**
     * 生产级预测算法服务
     */
    @Bean("predictiveAlgorithmProductionService")
    @Primary
    public IPredictiveAlgorithmService predictiveAlgorithmProductionService() {
        return new PredictiveAlgorithmProductionServiceImpl(callTaskService);
    }

    /**
     * 默认预测算法服务
     */
    @Bean("predictiveAlgorithmService")
    public IPredictiveAlgorithmService predictiveAlgorithmService() {
        return new PredictiveAlgorithmServiceImpl(callTaskService);
    }
}