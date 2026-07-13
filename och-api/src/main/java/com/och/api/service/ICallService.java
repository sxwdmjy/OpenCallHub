package com.och.api.service;

import com.och.system.domain.query.call.CallQuery;

public interface ICallService {

    /**
     * 拨打
     * @param query 呼叫参数
     * @return 会话ID
     */
    Long makeCall(CallQuery query);
}
