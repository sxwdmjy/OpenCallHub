package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.FsModules;
import com.och.system.domain.query.modules.FsModulesAddQuery;
import com.och.system.domain.query.modules.FsModulesQuery;
import com.och.system.mapper.FsModulesMapper;
import com.och.system.service.IFsModulesService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * fs模块管理表(FsModules)
 *
 * @author danmo
 * @date 2023-09-15 15:42:28
 */
@Service
public class FsModulesServiceImpl extends BaseServiceImpl<FsModulesMapper, FsModules> implements IFsModulesService {


    @Override
    public void add(FsModulesAddQuery query) {
        FsModules fsModules = new FsModules();
        BeanUtil.copyProperties(query, fsModules);
        save(fsModules);
    }

    @Override
    public void edit(FsModulesAddQuery query) {
        FsModules fsModules = new FsModules();
        BeanUtil.copyProperties(query, fsModules);
        update(fsModules, new LambdaQueryWrapper<FsModules>().eq(FsModules::getId, query.getId()));
    }

    @Override
    public FsModules getDetail(Long id) {
        return getById(id);
    }

    @Override
    public void delete(FsModulesQuery query) {
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
        List<FsModules> list = ids.stream().map(id -> {
            FsModules fsModules = new FsModules();
            fsModules.setId(id);
            fsModules.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return fsModules;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public List<FsModules> getList(FsModulesQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public FsModules getModuleByName(String name) {
        FsModulesQuery query = new FsModulesQuery();
        query.setName(name);
        List<FsModules> list = getList(query);
        if (CollectionUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<FsModules> getPageList(FsModulesQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }
}
