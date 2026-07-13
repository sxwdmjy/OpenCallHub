package com.och.call.service.impl;

import com.och.call.domain.CallRecord;
import com.och.call.domain.CallSession;
import com.och.call.domain.ServiceRecord;
import com.och.call.mapper.CallRecordMapper;
import com.och.call.mapper.CallSessionMapper;
import com.och.call.mapper.ServiceRecordMapper;
import com.och.security.audit.Audited;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 通话后处理服务
 * <p>
 * 负责通话结束后的业务闭环：创建通话归档记录、提交座席服务小结（处置结果、客户跟进、工单关联）。
 * 是通话生命周期从"通话结束"到"可查询报表"的关键桥接服务。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AfterCallWorkServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.call.mapper.ServiceRecordMapper, com.och.call.domain.ServiceRecord> implements com.och.call.service.IAfterCallWorkService {

    private final CallRecordMapper callRecordMapper;
    private final CallSessionMapper sessionMapper;

    /**
     * 提交后处理记录
     */
    @Transactional
    @Audited(action = "AFTER_CALL_WORK", targetType = "CALL")
    public ServiceRecord submitAfterCallWork(ServiceRecord serviceRecord) {
        // 查找关联的通话记录
        CallRecord callRecord = callRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CallRecord>()
                        .eq(CallRecord::getCallId, serviceRecord.getCallId()));
        if (callRecord != null) {
            serviceRecord.setCallRecordId(callRecord.getId());
            // 更新客户的关联
            if (serviceRecord.getCustomerId() != null) {
                callRecord.setCustomerId(serviceRecord.getCustomerId());
                callRecordMapper.updateById(callRecord);
            }
        }

        baseMapper.insert(serviceRecord);
        log.info("[AfterCallWork] Submitted: callId={}, agentId={}, disposition={}",
                serviceRecord.getCallId(), serviceRecord.getAgentId(), serviceRecord.getDisposition());
        return serviceRecord;
    }

    /**
     * 创建通话记录
     */
    @Transactional
    public CallRecord createCallRecord(CallSession session) {
        CallRecord record = CallRecord.builder()
                .sessionId(session.getId())
                .callId(session.getCallId())
                .callerNumber(session.getCallerNumber())
                .calledNumber(session.getCalledNumber())
                .direction(session.getDirection())
                .state(session.getState())
                .agentId(session.getAgentId())
                .queueId(session.getQueueId())
                .recordingUrl(session.getRecordingUrl())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .talkSeconds(session.getDurationSeconds())
                .endCause(session.getEndCause())
                .createdAt(LocalDateTime.now())
                .build();
        callRecordMapper.insert(record);
        return record;
    }
}
