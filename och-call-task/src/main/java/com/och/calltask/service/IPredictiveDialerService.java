package com.och.calltask.service;
import com.och.calltask.domain.OutboundDialAttempt;
public interface IPredictiveDialerService {
    double calculateDialRate(Long skillGroupId);
    OutboundDialAttempt dialNext(Long skillGroupId, Long taskId);
    void recordDialResult(OutboundDialAttempt attempt);
}
