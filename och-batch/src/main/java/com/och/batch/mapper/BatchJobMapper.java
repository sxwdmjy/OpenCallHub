package com.och.batch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.batch.domain.BatchJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 批处理任务 Mapper，基于 MyBatis-Plus BaseMapper。
 * <p>
 * 提供 batch_job 表的基础 CRUD 操作，包括增删改查以及分页查询能力。
 * 通过 MyBatis-Plus 的 {@link BaseMapper} 接口，可便捷地执行单表操作，
 * 无需编写 XML 映射文件。
 * </p>
 *
 * @author danmo
 */
@Mapper
public interface BatchJobMapper extends BaseMapper<BatchJob> {

}
