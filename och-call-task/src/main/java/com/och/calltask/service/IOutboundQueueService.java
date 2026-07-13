package com.och.calltask.service;
import com.och.calltask.domain.OutboundCustomer;
import java.util.List;
public interface IOutboundQueueService {
    OutboundCustomer enqueue(Long customerId);
    int batchEnqueue(Long taskId, List<Long> customerIds);
    OutboundCustomer dequeue(Long taskId);
    OutboundCustomer peek(Long taskId);
    int queueSize(Long taskId);
    void removeFromQueue(Long customerId);
    int drainQueue(Long taskId);
}
