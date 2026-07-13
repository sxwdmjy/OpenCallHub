package com.och.call.service;
import com.och.call.domain.CallRecord;
import com.och.call.domain.CallSession;
import com.och.call.domain.ServiceRecord;
public interface IAfterCallWorkService {
    ServiceRecord submitAfterCallWork(ServiceRecord serviceRecord);
    CallRecord createCallRecord(CallSession session);
}
