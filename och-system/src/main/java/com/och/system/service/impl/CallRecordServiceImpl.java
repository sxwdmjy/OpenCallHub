package com.och.system.service.impl;

import com.och.common.base.BaseServiceImpl;
import com.och.system.domain.query.call.CallRecordQuery;
import com.och.system.domain.vo.call.CallRecordVo;
import com.och.system.mapper.CallRecordMapper;
import com.och.system.domain.entity.CallRecord;
import com.och.system.service.ICallRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 呼叫记录表(CallRecord)表服务实现类
 *
 * @author danmo
 * @since 2024-11-21 11:23:02
 */
@Service
public class CallRecordServiceImpl extends BaseServiceImpl<CallRecordMapper, CallRecord> implements ICallRecordService {

    @Override
    public List<CallRecordVo> getCallPageList(CallRecordQuery query)
    {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return baseMapper.getCallPageList(query);
    }
}

