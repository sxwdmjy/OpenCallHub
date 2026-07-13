package com.och.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.quality.domain.QualityTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质检模板 Mapper，基于 MyBatis-Plus BaseMapper。
 * <p>
 * 继承 BaseMapper 后自动拥有 CRUD 基础能力，无需手写 SQL。
 * </p>
 *
 * @author danmo
 */
@Mapper
public interface QualityTemplateMapper extends BaseMapper<QualityTemplate> {
}
