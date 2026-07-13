package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.FsConfig;
import com.och.system.domain.query.fsconfig.FsConfigAddQuery;
import com.och.system.domain.query.fsconfig.FsConfigQuery;
import com.och.system.mapper.FsConfigMapper;
import com.och.system.service.IFsConfigService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * fs管理配置表(FsConfig)表服务实现类
 *
 * @author danmo
 * @since 2023-10-17 11:04:58
 */
@Service
public class FsConfigServiceImpl extends BaseServiceImpl<FsConfigMapper, FsConfig> implements IFsConfigService {

    @Override
    public void updateStatusById(Integer id, Integer status) {
        FsConfig fsFsConfig = new FsConfig();
        fsFsConfig.setStatus(status);
        fsFsConfig.setId(id);
        updateById(fsFsConfig);
    }

    @Override
    public void add(FsConfigAddQuery query) {
        FsConfig fsConfig = new FsConfig();
        BeanUtil.copyProperties(query, fsConfig);
        save(fsConfig);
    }

    @Override
    public void edit(FsConfigAddQuery query) {
        FsConfig fsConfig = new FsConfig();
        BeanUtil.copyProperties(query, fsConfig);
        updateById(fsConfig);
    }

    @Override
    public FsConfig getDetail(Integer id) {
        return getById(id);
    }

    @Override
    public void delete(FsConfigQuery query) {
        List<Integer> ids = new LinkedList<>();
        if (Objects.nonNull(query.getId())) {
            ids.add(query.getId());
        }
        if (CollectionUtil.isNotEmpty(query.getIds())) {
            ids.addAll(query.getIds());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        List<FsConfig> list = ids.stream().map(id -> {
            FsConfig fsConfig = new FsConfig();
            fsConfig.setId(id);
            fsConfig.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return fsConfig;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public List<FsConfig> getList(FsConfigQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public List<FsConfig> getPageList(FsConfigQuery query) {
        startPage(query.getPageIndex(), query.getPageSize());
        return getList(query);
    }
}

