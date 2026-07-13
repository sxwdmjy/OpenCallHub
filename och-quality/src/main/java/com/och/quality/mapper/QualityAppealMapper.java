package com.och.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.quality.domain.QualityAppeal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质检申诉 Mapper，基于 MyBatis-Plus BaseMapper。
 * <p>
 * 继承 BaseMapper 后自动拥有 CRUD 基础能力，用于申诉记录的持久化和查询。
 * </p>
 *
 * @author danmo
 */
@Mapper
public interface QualityAppealMapper extends BaseMapper<QualityAppeal> {
}
