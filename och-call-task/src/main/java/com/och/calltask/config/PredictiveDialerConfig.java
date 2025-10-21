package com.och.calltask.config;

import com.och.calltask.handler.PredictiveDialerHandler;
import com.och.calltask.service.*;
import com.och.system.service.ICallSkillService;
import com.och.system.service.ISipAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 预测式外呼配置类
 */
@Configuration
public class PredictiveDialerConfig {
    
    @Autowired
    private ICallTaskService callTaskService;
    
    @Autowired
    private ISipAgentService sipAgentService;
    
    @Autowired
    private ICallTaskAssignmentService callTaskAssignmentService;
    
    @Autowired
    private IPredictiveDialingService predictiveDialingService;
    
    @Autowired
    private ICallQueueService callQueueService;
    
    @Autowired
    private ICallSkillService callSkillService;
    
    @Autowired
    private IPredictiveAlgorithmService predictiveAlgorithmService;

    @Bean
    public PredictiveDialerHandler predictiveDialerHandler() {
        PredictiveDialerHandler handler = new PredictiveDialerHandler(
                callTaskService,
                sipAgentService,
                callTaskAssignmentService,
                predictiveDialingService,
                callQueueService,
                callSkillService);
        
        handler.setPredictiveAlgorithmService(predictiveAlgorithmService);
        return handler;
    }
}