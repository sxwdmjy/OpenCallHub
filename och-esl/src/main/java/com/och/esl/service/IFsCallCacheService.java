package com.och.esl.service;


import com.och.common.domain.CallInfo;
import com.och.system.domain.vo.route.CallRouteVo;

/**
 * @author danmo
 * @date 2023-10-23 16:43
 **/
public interface IFsCallCacheService {

    void saveCallInfo(CallInfo callInfo);

    CallInfo getCallInfo(Long callId);

    CallInfo getCallInfoByUniqueId(String uniqueId);

    void saveCallRel(String uniqueId, Long callId);

    Long getCallId(String uniqueId);

    /**
     * 获取路由信息
     * @param routeNum 路由号码
     * @param type 路由类型 1-呼入 2-呼出
     * @return 路由信息
     */
    CallRouteVo getCallRoute(String routeNum, Integer type);


    void removeCallInfo(Long callId);
}
