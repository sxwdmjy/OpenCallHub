package com.och.calltask.service;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundDialAttempt;
public interface IPreviewDialingService {
    OutboundCustomer claimCustomer(Long taskId, Long agentId);
    void releaseCustomer(Long customerId, Long agentId);
    OutboundDialAttempt startDial(Long taskId, Long customerId, Long agentId);
}
