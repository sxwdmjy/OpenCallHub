package com.och.call.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.call.domain.CallSession;
import org.apache.ibatis.annotations.Mapper;

/** 通话会话数据访问层 */
@Mapper
public interface CallSessionMapper extends BaseMapper<CallSession> {
}
