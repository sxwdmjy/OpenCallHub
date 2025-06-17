package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.system.domain.entity.OchFieldInfo;
import com.och.system.domain.query.calltask.FieldAddQuery;
import com.och.system.domain.query.calltask.FieldQuery;
import com.och.system.domain.vo.calltask.FieldInfoVo;
import com.och.system.mapper.OchFieldInfoMapper;
import com.och.system.service.IOchFieldInfoService;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字段管理表(OchFieldInfo)表服务实现类
 *
 * @author danmo
 * @since 2025-06-16 14:53:21
 */
@RequiredArgsConstructor
@Service
public class OchFieldInfoServiceImpl extends BaseServiceImpl<OchFieldInfoMapper, OchFieldInfo> implements IOchFieldInfoService {

    private final ISysUserService sysUserService;

    @Override
    public void add(FieldAddQuery query) {
        OchFieldInfo ochFieldInfo = new OchFieldInfo();
        BeanUtils.copyProperties(query, ochFieldInfo);
        save(ochFieldInfo);
    }

    @Override
    public void edit(FieldAddQuery query) {
        OchFieldInfo fieldInfo = getById(query.getId());
        if (Objects.isNull(fieldInfo)) {
            throw new CommonException("无效ID");
        }
        OchFieldInfo ochFieldInfo = new OchFieldInfo();
        BeanUtils.copyProperties(query, ochFieldInfo);
        updateById(ochFieldInfo);
    }

    @Override
    public FieldInfoVo get(Long id) {
        OchFieldInfo fieldInfo = getById(id);
        if (Objects.isNull(fieldInfo)) {
            throw new CommonException("无效ID");
        }
        FieldInfoVo fieldInfoVo = new FieldInfoVo();
        BeanUtils.copyProperties(fieldInfo, fieldInfoVo);
        sysUserService.decorate(fieldInfoVo);
        return fieldInfoVo;
    }

    @Override
    public void delete(FieldQuery query) {
        List<Long> ids = new LinkedList<>();
        if (Objects.nonNull(query.getId())) {
            ids.add(query.getId());
        }
        if (CollectionUtil.isNotEmpty(query.getIdList())) {
            ids.addAll(query.getIdList());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        List<OchFieldInfo> list = ids.stream().map(id -> {
            OchFieldInfo fieldInfo = new OchFieldInfo();
            fieldInfo.setId(id);
            fieldInfo.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return fieldInfo;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public List<FieldInfoVo> pageList(FieldQuery query) {
        startPage(query.getPageIndex(), query.getPageSize());
        List<FieldInfoVo> fieldInfoList = getList(query);
        if (CollectionUtil.isNotEmpty(fieldInfoList)) {
            sysUserService.decorate(fieldInfoList);
        }
        return fieldInfoList;
    }

    @Override
    public List<FieldInfoVo> getList(FieldQuery query) {
        return this.baseMapper.getList(query);
    }
}

