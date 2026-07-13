package com.och.batch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.batch.domain.BatchJobItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 批处理条目明细 Mapper，基于 MyBatis-Plus BaseMapper。
 * 支持按任务ID和状态查询条目明细。
 */
@Mapper
public interface BatchJobItemMapper extends BaseMapper<BatchJobItem> {
}
