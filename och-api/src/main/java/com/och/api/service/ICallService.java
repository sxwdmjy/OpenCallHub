package com.och.api.service;


import com.och.system.domain.query.call.CallQuery;
import com.och.system.domain.query.call.CallRecordQuery;
import com.och.system.domain.vo.call.CallRecordVo;

import java.util.List;

public interface ICallService {

    /**
     * 拨打
     * @param query
     * @return
     */
    Long makeCall(CallQuery query);

    /**
     * 获取呼叫详情
     * @param callId
     * @return
     */
    CallRecordVo getCallInfo(Long callId);

    /**
     * 获取呼叫列表
     * @param query
     * @return
     */
    List<CallRecordVo> getCallPageList(CallRecordQuery query);
}
