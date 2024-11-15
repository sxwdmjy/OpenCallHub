package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.utils.StringUtils;
import com.och.system.domain.entity.CallDisplay;
import com.och.system.domain.query.display.CallDisplayAddQuery;
import com.och.system.domain.query.display.CallDisplayQuery;
import com.och.system.domain.vo.display.CallDisplaySimpleVo;
import com.och.system.domain.vo.display.CallDisplayVo;
import com.och.system.mapper.CallDisplayMapper;
import com.och.system.service.ICallDisplayService;
import com.och.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 号码管理(CallDisplay)表服务实现类
 *
 * @author danmo
 * @since 2023-10-23 10:45:58
 */
@Service
public class CallDisplayServiceImpl extends BaseServiceImpl<CallDisplayMapper, CallDisplay> implements ICallDisplayService {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public void add(CallDisplayAddQuery query) {
        checkPhone(query.getPhone());
        CallDisplay callDisplay = new CallDisplay();
        BeanUtils.copyProperties(query, callDisplay);
        save(callDisplay);
    }

    @Override
    public void edit(CallDisplayAddQuery query) {
        CallDisplay display = getById(query.getId());
        if (Objects.isNull(display)) {
            throw new RuntimeException("无效ID");
        }
        if (!StringUtils.equals(query.getPhone(), display.getPhone())) {
            display.setPhone(query.getPhone());
        }
        if (Objects.nonNull(query.getType())) {
            display.setType(query.getType());
        }
        checkPhone(query.getPhone());
        updateById(display);
    }

    @Override
    public CallDisplayVo getDetail(Long id) {
        CallDisplayVo callDisplayVo = new CallDisplayVo();
        CallDisplay detail = getById(id);
        BeanUtils.copyProperties(detail, callDisplayVo);
        return callDisplayVo;
    }

    @Override
    public void delete(CallDisplayQuery query) {
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
        List<CallDisplay> list = ids.stream().map(id -> {
            CallDisplay display = new CallDisplay();
            display.setId(id);
            display.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return display;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public List<CallDisplayVo> getList(CallDisplayQuery query) {
        return baseMapper.getList(query);
    }

    @Override
    public void allocate(CallDisplayQuery query) {

    }

    @Override
    public List<CallDisplayVo> getPageList(CallDisplayQuery query) {
        startPage(query.getPageIndex(), query.getPageSize());
        List<CallDisplayVo> displayList = getList(query);
        if (CollectionUtil.isNotEmpty(displayList)) {
            sysUserService.decorate(displayList);
        }
        return displayList;
    }

    @Override
    public List<CallDisplaySimpleVo> selectSimpleList(CallDisplayQuery query) {
        List<CallDisplayVo> list = getList(query);
        if (CollectionUtil.isNotEmpty(list)) {
            return list.stream().map(displayVo -> {
                CallDisplaySimpleVo simpleVo = new CallDisplaySimpleVo();
                simpleVo.setDisplayId(displayVo.getId());
                simpleVo.setDisplayNumber(displayVo.getPhone());
                return simpleVo;
            }).toList();
        }
        return new ArrayList<>();
    }

    private void checkPhone(String phone) {
        CallDisplayQuery callDisplayQuery = new CallDisplayQuery();
        callDisplayQuery.setPhone(phone);
        List<CallDisplayVo> list = getList(callDisplayQuery);
        if (CollectionUtil.isNotEmpty(list)) {
            throw new RuntimeException("号码已存在");
        }
    }
}

