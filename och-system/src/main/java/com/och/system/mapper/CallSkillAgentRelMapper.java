package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.CallSkillAgentRel;

/**
 * 技能坐席关联表(CallSkillAgentRel)表数据库访问层
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
@Repository()
@Mapper
public interface CallSkillAgentRelMapper extends BaseMapper<CallSkillAgentRel> {

}

