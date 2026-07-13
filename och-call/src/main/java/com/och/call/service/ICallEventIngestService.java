package com.och.call.service;
import com.och.call.domain.CallEvent;
import com.och.esl.domain.NormalizedEslEvent;
public interface ICallEventIngestService {
    void onNormalizedEslEvent(NormalizedEslEvent eslEvent);
    void ingest(NormalizedEslEvent eslEvent);
    void ingest(CallEvent event);
}
