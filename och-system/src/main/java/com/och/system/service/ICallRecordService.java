package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallRecord;
import com.och.system.domain.query.call.CallRecordQuery;
import com.och.system.domain.vo.call.CallRecordVo;

import java.util.List;

/**
 * 呼叫记录表(CallRecord)表服务接口
 *
 * @author danmo
 * @since 2024-11-21 11:23:01
 */
public interface ICallRecordService extends IBaseService<CallRecord> {

    List<CallRecordVo> getCallPageList(CallRecordQuery query);
}

