package com.och.system.service;

import com.github.pagehelper.PageInfo;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;

import java.util.List;

/**
 * 技能组管理服务接口
 * <p>提供技能组的增删改查与坐席分配管理功能。
 * ACD 技能组分配支持多种分配策略（空闲优先/轮询/最少通话），实现智能的坐席调度。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>策略模式</b>：ACD 技能组分配采用不同分配策略（空闲优先/轮询/最少通话），可根据业务需求灵活切换</li>
 *   <li><b>模板方法</b>：继承 IBaseService，复用通用 CRUD 模板</li>
 * </ul>
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

