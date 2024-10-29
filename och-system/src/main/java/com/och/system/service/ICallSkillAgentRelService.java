package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallSkillAgentRel;

import java.util.List;

/**
 * 技能坐席关联表(CallSkillAgentRel)表服务接口
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
public interface ICallSkillAgentRelService extends IBaseService<CallSkillAgentRel> {

    void addBySkillId(Long skillId, List<CallSkillAgentRel> agentList);

    void updateBySkillId(Long skillId, List<CallSkillAgentRel> agentList);

    void deleteBySkillId(List<Long> skillIds);
}

