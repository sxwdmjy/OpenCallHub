package com.och.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.knowledge.domain.ScriptTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话术模板 Mapper，基于 MyBatis-Plus BaseMapper。
 * <p>
 * 继承 BaseMapper 后自动拥有 CRUD 基础能力。
 * 按关键字筛选话术通过 {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 匹配名称或关键词字段。
 * </p>
 *
 * @author danmo
 */
@Mapper
public interface ScriptTemplateMapper extends BaseMapper<ScriptTemplate> {
}
