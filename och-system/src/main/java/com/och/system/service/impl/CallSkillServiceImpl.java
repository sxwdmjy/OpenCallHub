package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageInfo;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;
import com.och.system.mapper.CallSkillMapper;
import com.och.system.service.ICallSkillAgentRelService;
import com.och.system.service.ICallSkillService;
import com.och.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 技能表(CallSkill)表服务实现类
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
@Service
public class CallSkillServiceImpl extends BaseServiceImpl<CallSkillMapper, CallSkill> implements ICallSkillService {

    @Autowired
    private ICallSkillAgentRelService iCallSkillAgentRelService;
    @Autowired
    private ISysUserService iSysUserService;

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
            CallSkill lfsSkill = new CallSkill();
            lfsSkill.setId(id);
            lfsSkill.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return lfsSkill;
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
        CallSkillQuery lfsSkillQuery = new CallSkillQuery();
        lfsSkillQuery.setIds(ids);
        startPage(query.getPageIndex(), query.getPageSize(),query.getSortField(),query.getSort());
        List<CallSkillVo> list = getList(lfsSkillQuery);

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


}

