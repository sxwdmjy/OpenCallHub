package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageInfo;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallDisplayPool;
import com.och.system.domain.query.display.CallDisplayPoolAddQuery;
import com.och.system.domain.query.display.CallDisplayPoolQuery;
import com.och.system.domain.vo.display.CallDisplayPoolVo;
import com.och.system.mapper.CallDisplayPoolMapper;
import com.och.system.service.ICallDisplayPoolRelService;
import com.och.system.service.ICallDisplayPoolService;
import com.och.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 号码池管理(CallDisplayPool)表服务实现类
 *
 * @author danmo
 * @since 2023-11-03 14:57:17
 */
@Service
public class CallDisplayPoolServiceImpl extends BaseServiceImpl<CallDisplayPoolMapper, CallDisplayPool> implements ICallDisplayPoolService {

    @Autowired
    private ICallDisplayPoolRelService iCallDisplayPoolRelService;

    @Autowired
    private ISysUserService iSysUserService;

    @Override
    public void addPool(CallDisplayPoolAddQuery query) {
        CallDisplayPool CallDisplayPool = new CallDisplayPool();
        CallDisplayPool.setName(query.getName());
        CallDisplayPool.setType(query.getType());
        if (save(CallDisplayPool)) {
            iCallDisplayPoolRelService.addByPoolId(CallDisplayPool.getId(), query.getPhoneList());
        }
    }

    @Override
    public void editPool(CallDisplayPoolAddQuery query) {
        CallDisplayPool displayPool = getById(query.getId());
        if(Objects.isNull(displayPool)){
            throw new RuntimeException("无效ID");
        }
        CallDisplayPool CallDisplayPool = new CallDisplayPool();
        CallDisplayPool.setId(query.getId());
        CallDisplayPool.setName(query.getName());
        CallDisplayPool.setType(query.getType());
        if (updateById(CallDisplayPool)) {
            iCallDisplayPoolRelService.editByPoolId(query.getId(), query.getPhoneList());
        }
    }

    @Override
    public void deletePool(CallDisplayPoolQuery query) {
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
        List<CallDisplayPool> list = ids.stream().map(id -> {
            CallDisplayPool pool = new CallDisplayPool();
            pool.setId(id);
            pool.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return pool;
        }).collect(Collectors.toList());
        if (updateBatchById(list)) {
            iCallDisplayPoolRelService.delByPoolId(query.getId());
        }
    }

    @Override
    public CallDisplayPoolVo getPoolDetail(Long id) {
        return this.baseMapper.getPoolDetail(id);
    }

    @Override
    public PageInfo<CallDisplayPoolVo> pagePoolList(CallDisplayPoolQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        List<CallDisplayPoolVo> poolList = new LinkedList<>();
        List<Long> poolIds = this.baseMapper.selectIdsByQuery(query);
        if (CollectionUtil.isNotEmpty(poolIds)) {
            query.setIds(poolIds);
            List<CallDisplayPoolVo> pageList = getPoolList(query);
            poolList.addAll(pageList);
        }
        PageInfo<Long> pageTempInfo = new PageInfo<>(poolIds);
        PageInfo<CallDisplayPoolVo> pageInfo = new PageInfo<>(poolList);
        pageInfo.setTotal(pageTempInfo.getTotal());
        pageInfo.setPageNum(pageTempInfo.getPageNum());
        pageInfo.setPageSize(pageTempInfo.getPageSize());
        return pageInfo;
    }

    @Override
    public List<CallDisplayPoolVo> getPoolList(CallDisplayPoolQuery query) {
        return this.baseMapper.getList(query);
    }
}

