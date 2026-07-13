package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.entity.CallSkillAgentRel;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;
import com.och.system.mapper.CallSkillMapper;
import com.och.system.service.ICallSkillAgentRelService;
import com.och.system.service.ICallSkillService;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 技能组管理服务
 * <p>负责呼叫技能组的增删改查操作，包括技能组与坐席的关联关系维护</p>
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
@RequiredArgsConstructor
@Service
public class CallSkillServiceImpl extends BaseServiceImpl<CallSkillMapper, CallSkill> implements ICallSkillService {

    private final ICallSkillAgentRelService iCallSkillAgentRelService;
    private final ISysUserService iSysUserService;

    @Override
    public void add(CallSkillAddQuery query) {
        CallSkill skill = new CallSkill();
        skill.setQuery2Entity(query);
        if (save(skill)) {
            iCallSkillAgentRelService.addBySkillId(skill.getId(), query.getAgentList());
        }
    }

    @Override
    public void edit(CallSkillAddQuery query) {
        CallSkill skill = new CallSkill();
        skill.setQuery2Entity(query);
        if (updateById(skill)) {
            iCallSkillAgentRelService.updateBySkillId(skill.getId(), query.getAgentList());
        }
    }

    @Override
    public void delete(CallSkillQuery query) {
        List<Long> ids = new LinkedList<>();
        if (Objects.nonNull(query.getId())) {
            ids.add(query.getId());
        }
        if (CollectionUtil.isNotEmpty(query.getIds())) {
            ids.addAll(query.getIds());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        List<CallSkill> list = ids.stream().map(id -> {
            CallSkill skill = new CallSkill();
            skill.setId(id);
            skill.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return skill;
        }).collect(Collectors.toList());
        if(updateBatchById(list)){
            iCallSkillAgentRelService.deleteBySkillId(ids);
        }
    }

    @Override
    public CallSkillVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public PageInfo<CallSkillVo> pageList(CallSkillQuery query) {
        List<Long> ids = this.baseMapper.getIdsByQuery(query);
        if (CollectionUtil.isEmpty(ids)) {
            return new PageInfo<>(new LinkedList<>());
        }
        CallSkillQuery skillQuery = new CallSkillQuery();
        skillQuery.setIds(ids);
        startPage(query.getPageIndex(), query.getPageSize(),query.getSortField(),query.getSort());
        List<CallSkillVo> list = getList(skillQuery);

        PageInfo<Long> pageIdInfo = new PageInfo<>(ids);
        PageInfo<CallSkillVo> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(pageIdInfo.getTotal());
        pageInfo.setPageNum(pageIdInfo.getPageNum());
        pageInfo.setPageSize(pageIdInfo.getPageSize());
        if(CollectionUtil.isNotEmpty(pageInfo.getList())){
            iSysUserService.decorate(pageInfo.getList());
        }
        return pageInfo;
    }


    @Override
    public List<CallSkillVo> getList(CallSkillQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public List<CallSkillVo> getListByIds(CallSkillQuery query) {
        return this.baseMapper.getListByIds(query.getIds());
    }

    @Override
    public List<Long> getAgentListBySkillId(Long skillId) {
        List<CallSkillAgentRel> agentRelList = iCallSkillAgentRelService.list(new LambdaQueryWrapper<CallSkillAgentRel>()
                .eq(CallSkillAgentRel::getSkillId, skillId)
                .eq(CallSkillAgentRel::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
       return Optional.of(agentRelList).orElse(
                new LinkedList<>()
        ).stream().map(CallSkillAgentRel::getAgentId).toList();
    }


}

