package com.och.acd.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.acd.domain.SkillGroup;
import com.och.acd.mapper.SkillGroupMapper;
import com.och.acd.service.ISkillGroupService;
import org.springframework.stereotype.Service;

/**
 * 技能组服务实现
 *
 * @author danmo
 */
@Service
@RequiredArgsConstructor
public class SkillGroupServiceImpl implements ISkillGroupService {

    private final SkillGroupMapper skillGroupMapper;

    @Override
    public IPage<SkillGroup> pageList(int pageNum, int pageSize) {
        return skillGroupMapper.selectPage(new Page<>(pageNum, pageSize), null);
    }

    @Override
    public SkillGroup create(SkillGroup group) {
        skillGroupMapper.insert(group);
        return group;
    }

    @Override
    public SkillGroup update(Long id, SkillGroup group) {
        group.setId(id);
        skillGroupMapper.updateById(group);
        return group;
    }

    @Override
    public void delete(Long id) {
        skillGroupMapper.deleteById(id);
    }
}