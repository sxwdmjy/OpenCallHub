package com.och.call.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.call.domain.CallEvent;
import com.och.call.mapper.CallEventMapper;
import com.och.call.service.ICallEventService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通话事件服务实现
 *
 * @author danmo
 */
@Service
@RequiredArgsConstructor
public class CallEventServiceImpl implements ICallEventService {

    private final CallEventMapper eventMapper;

    @Override
    public List<CallEvent> listByCallId(String callId) {
        LambdaQueryWrapper<CallEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CallEvent::getCallId, callId).orderByAsc(CallEvent::getEventTime);
        return eventMapper.selectList(wrapper);
    }
}