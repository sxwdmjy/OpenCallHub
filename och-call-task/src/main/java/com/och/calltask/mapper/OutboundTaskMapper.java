package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.OutboundTask;
import org.apache.ibatis.annotations.Mapper;

/** 外呼任务数据访问层 */
@Mapper
public interface OutboundTaskMapper extends BaseMapper<OutboundTask> {}
