package com.och.call.service;
import com.och.call.domain.CallRecord;
import com.och.call.domain.ServiceRecord;
import java.util.List;
public interface IPhoneBarService {
    void answer(String callId);
    void hangup(String callId, String cause);
    void hold(String callId);
    void unhold(String callId);
    void blindTransfer(String callId, String targetNumber);
    void consultTransfer(String callId, String targetNumber);
    ServiceRecord submitAfterCallWork(ServiceRecord record);
    List<CallRecord> listCallRecords(Long agentId);
    List<ServiceRecord> listServiceRecords(String callId);
}
