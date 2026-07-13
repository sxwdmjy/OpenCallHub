package com.och.call.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.call.domain.CallRecord;
import org.apache.ibatis.annotations.Mapper;

/** 通话记录数据访问层 */
@Mapper
public interface CallRecordMapper extends BaseMapper<CallRecord> {
}
