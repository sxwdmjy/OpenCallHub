package com.och.calltask.service;
import java.util.Map;
public interface ICustomerCrowdService {
    com.och.calltask.domain.CustomerCrowd createCrowd(String name, Long poolId, String filterCondition);
    int calculateCrowd(Long crowdId);
    Map<String, Object> getCrowdCustomers(Long crowdId);
    Map<String, Object> getCrowdStats(Long crowdId);
    int assignToTask(Long crowdId, Long taskId);
}
