package com.och.system.service;

import com.github.pagehelper.PageInfo;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;

import java.util.List;

/**
 * 技能表(CallSkill)表服务接口
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
public interface ICallSkillService extends IBaseService<CallSkill> {

    void add(CallSkillAddQuery query);

    void edit(CallSkillAddQuery query);

    void delete(CallSkillQuery query);

    CallSkillVo getDetail(Long id);

    PageInfo<CallSkillVo> pageList(CallSkillQuery query);

    List<CallSkillVo> getList(CallSkillQuery query);

    List<CallSkillVo> getListByIds(CallSkillQuery query);

    List<Long> getAgentListBySkillId(Long skillId);
}

