package com.och.call.service;

import com.och.call.domain.CallEvent;

import java.util.List;

/**
 * 通话事件服务接口
 * <p>
 * 提供通话事件的查询能力，按 callId 获取事件时间线。
 * </p>
 *
 * @author danmo
 */
public interface ICallEventService {

    /**
     * 根据呼叫ID获取事件列表（按事件时间升序排列）
     *
     * @param callId 呼叫全局唯一标识
     * @return 事件列表
     */
    List<CallEvent> listByCallId(String callId);
}