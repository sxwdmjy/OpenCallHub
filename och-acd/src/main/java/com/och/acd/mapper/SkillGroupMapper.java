package com.och.acd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.acd.domain.SkillGroup;
import org.apache.ibatis.annotations.Mapper;

/** 技能组数据访问层 */
@Mapper
public interface SkillGroupMapper extends BaseMapper<SkillGroup> {
}
