package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.mapper.CallSkillAgentRelMapper;
import com.och.system.domain.entity.CallSkillAgentRel;
import com.och.system.service.ICallSkillAgentRelService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 技能坐席关联表(CallSkillAgentRel)表服务实现类
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
@Service
public class CallSkillAgentRelServiceImpl extends BaseServiceImpl<CallSkillAgentRelMapper, CallSkillAgentRel> implements ICallSkillAgentRelService {

    @Override
    public void addBySkillId(Long skillId, List<CallSkillAgentRel> agentList) {
        if(CollectionUtil.isEmpty(agentList)){
            return;
        }
        agentList.forEach(agent -> agent.setSkillId(skillId));
        saveBatch(agentList);
    }

    @Override
    public void updateBySkillId(Long skillId, List<CallSkillAgentRel> agentList) {
        if(CollectionUtil.isEmpty(agentList)){
            return;
        }
        deleteBySkillId(Collections.singletonList(skillId));
        addBySkillId(skillId,agentList);
    }

    @Override
    public void deleteBySkillId(List<Long> skillIds) {
        if(CollectionUtil.isEmpty(skillIds)){
            return;
        }
        CallSkillAgentRel skillAgentRel = new CallSkillAgentRel();
        skillAgentRel.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        update(skillAgentRel,new LambdaQueryWrapper<CallSkillAgentRel>().in(CallSkillAgentRel::getSkillId,skillIds));
    }
}

