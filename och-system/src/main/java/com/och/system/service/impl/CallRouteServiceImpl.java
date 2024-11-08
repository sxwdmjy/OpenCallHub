package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallRoute;
import com.och.system.domain.query.route.CallRouteAddQuery;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.vo.route.CallRouteListVo;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.mapper.CallRouteMapper;
import com.och.system.service.ICallRouteRelService;
import com.och.system.service.ICallRouteService;
import com.och.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 号码路由表(CallRoute)表服务实现类
 *
 * @author danmo
 * @since 2023-10-18 14:21:22
 */
@Service
public class CallRouteServiceImpl extends BaseServiceImpl<CallRouteMapper, CallRoute> implements ICallRouteService {

    @Autowired
    private ICallRouteRelService iCallRouteRelService;
    @Autowired
    private ISysUserService iSysUserService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(CallRouteAddQuery query) {
        CallRoute lfsCallRoute = new CallRoute();
        lfsCallRoute.addQuery2Entity(query);
        if (save(lfsCallRoute)) {
            iCallRouteRelService.add(lfsCallRoute.getId(), query.getRouteRelList());
        }
    }

    @Override
    public void edit(CallRouteAddQuery query) {
        CallRoute lfsCallRoute = new CallRoute();
        lfsCallRoute.addQuery2Entity(query);
        if (updateById(lfsCallRoute)) {
            iCallRouteRelService.update(lfsCallRoute.getId(), query.getRouteRelList());
        }
    }

    @Override
    public void delete(CallRouteQuery query) {
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
        List<CallRoute> list = ids.stream().map(id -> {
            CallRoute lfsCallRoute = new CallRoute();
            lfsCallRoute.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            lfsCallRoute.setId(id);
            return lfsCallRoute;
        }).collect(Collectors.toList());
        if (updateBatchById(list)) {
            iCallRouteRelService.delete(ids);
        }
    }

    @Override
    public CallRouteVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public List<CallRouteListVo> getList(CallRouteQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public List<CallRouteListVo> getPageList(CallRouteQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        List<CallRouteListVo> list = getList(query);
        if(CollectionUtil.isNotEmpty(list)){
            iSysUserService.decorate(list);
        }
        return list;
    }

    @Override
    public List<CallRouteVo> listByQuery(CallRouteQuery query) {
        return this.baseMapper.listByQuery(query);
    }
}

