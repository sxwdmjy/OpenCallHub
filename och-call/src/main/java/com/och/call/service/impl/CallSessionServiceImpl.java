package com.och.call.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.call.domain.CallSession;
import com.och.call.mapper.CallSessionMapper;
import com.och.call.service.ICallSessionService;
import org.springframework.stereotype.Service;

/**
 * 通话会话服务实现
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionServiceImpl implements ICallSessionService {

    private final CallSessionMapper sessionMapper;

    @Override
    public IPage<CallSession> pageList(int pageNum, int pageSize, String state, String keyword) {
        LambdaQueryWrapper<CallSession> wrapper = new LambdaQueryWrapper<>();
        if (state != null && !state.isEmpty()) {
            wrapper.eq(CallSession::getState, state);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(CallSession::getCallerNumber, keyword)
                    .or().like(CallSession::getCalledNumber, keyword)
                    .or().like(CallSession::getCallId, keyword));
        }
        wrapper.orderByDesc(CallSession::getStartTime);
        return sessionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public CallSession getById(Long id) {
        return sessionMapper.selectById(id);
    }
}