package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.FsDialplan;
import com.och.system.domain.query.dialplan.FsDialplanAddQuery;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.vo.dialplan.FsDialplanVo;
import com.och.system.mapper.FsDialplanMapper;
import com.och.system.service.IFsDialplanService;
import com.och.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * fs拨号计划表(FsDialplan)
 *
 * @author danmo
 * @date 2023-09-15 11:04:20
 */
@Service
public class FsDialplanServiceImpl extends BaseServiceImpl<FsDialplanMapper, FsDialplan> implements IFsDialplanService {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public void add(FsDialplanAddQuery query) {
        FsDialplan fsDialplan = new FsDialplan();
        BeanUtil.copyProperties(query, fsDialplan);
        save(fsDialplan);
    }

    @Override
    public void edit(FsDialplanAddQuery query) {
        FsDialplan fsDialplan = new FsDialplan();
        BeanUtil.copyProperties(query, fsDialplan);
        update(fsDialplan, new LambdaQueryWrapper<FsDialplan>().eq(FsDialplan::getId, query.getId()));
    }

    @Override
    public FsDialplan getDetail(Long id) {
        return getById(id);
    }

    @Override
    public void delete(FsDialplanQuery query) {
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
        List<FsDialplan> list = ids.stream().map(id -> {
            FsDialplan dialplan = new FsDialplan();
            dialplan.setId(id);
            dialplan.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return dialplan;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public List<FsDialplanVo> getList(FsDialplanQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public List<FsDialplanVo> getPageList(FsDialplanQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(),query.getSortField(),query.getSort());
        List<FsDialplanVo> list = getList(query);
        sysUserService.decorate(list);
        return list;
    }
}
