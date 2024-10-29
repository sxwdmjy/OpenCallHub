package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 技能表(CallSkill)表数据库访问层
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
@Repository()
@Mapper
public interface CallSkillMapper extends BaseMapper<CallSkill> {

    CallSkillVo getDetail(@Param("id") Long id);

    List<Long> getIdsByQuery(CallSkillQuery query);

    List<CallSkillVo> getList(CallSkillQuery query);

    List<CallSkillVo> getListByIds(@Param("ids") List<Long> ids);
}

