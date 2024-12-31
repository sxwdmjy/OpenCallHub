package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.common.utils.StringUtils;
import com.och.system.domain.entity.CallRoute;
import com.och.system.domain.query.route.CallRouteAddQuery;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.mapper.CallRouteMapper;
import com.och.system.service.ICallRouteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 号码路由表(CallRoute)表服务实现类
 *
 * @author danmo
 * @since 2024-12-30 14:03:42
 */
@Service
public class CallRouteServiceImpl extends BaseServiceImpl<CallRouteMapper, CallRoute> implements ICallRouteService {

    @Override
    public void add(CallRouteAddQuery query) {
        if(checkName(query.getName())){
            throw new CommonException("名称已存在");
        }
        CallRoute route = new CallRoute();
        BeanUtil.copyProperties(query, route);
        route.setStatus(0);
        save(route);
    }

    @Override
    public void edit(CallRouteAddQuery query) {
        CallRoute callRoute = getById(query.getId());
        if (Objects.isNull(callRoute)){
            throw new CommonException("无效ID");
        }
        if (callRoute.getStatus() == 1){
            throw new CommonException("已启用的号码路由无法修改");
        }
        if(!StringUtils.equals(callRoute.getName(), query.getName()) && checkName(query.getName())){
            throw new CommonException("名称已存在");
        }
        CallRoute updateRoute = new CallRoute();
        BeanUtil.copyProperties(query, updateRoute);
        updateById(updateRoute);

    }



    @Override
    public void delete(CallRouteQuery query) {
        CallRoute callRoute = getById(query.getId());
        if (Objects.isNull(callRoute)){
            throw new CommonException("无效ID");
        }
        if (callRoute.getStatus() == 1){
            throw new CommonException("已启用的号码路由无法删除");
        }
        CallRoute route = new CallRoute();
        route.setId(query.getId());
        route.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        updateById(route);
    }

    @Override
    public CallRouteVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public List<CallRouteVo> getPageList(CallRouteQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }

    @Override
    public List<CallRouteVo> getList(CallRouteQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public void enable(Long id) {
        CallRoute callRoute = getById(id);
        if (Objects.isNull(callRoute)){
            throw new CommonException("无效ID");
        }
        if (callRoute.getStatus() == 1){
            throw new CommonException("已启用的号码路由无法启用");
        }
        CallRoute route = new CallRoute();
        route.setId(id);
        route.setStatus(1);
        updateById(route);
    }

    @Override
    public void disable(Long id) {
        CallRoute callRoute = getById(id);
        if (Objects.isNull(callRoute)){
            throw new CommonException("无效ID");
        }
        if (callRoute.getStatus() == 0){
            throw new CommonException("已禁用的号码路由无法禁用");
        }
        CallRoute route = new CallRoute();
        route.setId(id);
        route.setStatus(0);
        updateById(route);
    }

    /**
     * 检查名称是否重复
     * @param name 名称
     * @return 是否重复
     */
    private Boolean checkName(String name) {
        long count = count(new LambdaQueryWrapper<CallRoute>().eq(CallRoute::getName, name).eq(BaseEntity::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        if (count > 0){
            return true;
        }else {
            return false;
        }
    }
}

