package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.KoDispatcher;
import com.och.system.domain.query.dispatcher.KoDispatcherAddQuery;
import com.och.system.domain.query.dispatcher.KoDispatcherQuery;
import com.och.system.mapper.KoDispatcherMapper;
import com.och.system.service.IKoDispatcherService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (Dispatcher)
 *
 * @author danmo
 * @date 2023-08-29 10:49:18
 */
@Service
public class KoDispatcherServiceImpl extends BaseServiceImpl<KoDispatcherMapper, KoDispatcher> implements IKoDispatcherService {


    @Override
    public void add(KoDispatcherAddQuery query) {
        KoDispatcher dispatcher = new KoDispatcher();
        BeanUtil.copyProperties(query, dispatcher);
        dispatcher.setSetid(query.getGroupId());
        save(dispatcher);
    }

    @Override
    public void edit(KoDispatcherAddQuery query) {
        KoDispatcher dispatcher = new KoDispatcher();
        BeanUtil.copyProperties(query, dispatcher);
        dispatcher.setSetid(query.getGroupId());
        dispatcher.setId(query.getId());
        updateById(dispatcher);
    }

    @Override
    public void delete(KoDispatcherQuery query) {
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
        List<KoDispatcher> list = ids.stream().map(id -> {
            KoDispatcher dispatcher = new KoDispatcher();
            dispatcher.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            dispatcher.setId(id);
            return dispatcher;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public KoDispatcher getDetail(Integer id) {
        return getById(id);
    }

    @Override
    public List<KoDispatcher> getList(KoDispatcherQuery query) {
        LambdaQueryWrapper<KoDispatcher> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(query.getIds())) {
            wrapper.in(KoDispatcher::getId, query.getIds());
        }
        if (Objects.nonNull(query.getId())) {
            wrapper.eq(KoDispatcher::getId, query.getId());
        }
        if (Objects.nonNull(query.getGroupId())) {
            wrapper.eq(KoDispatcher::getSetid, query.getGroupId());
        }
        if (Objects.nonNull(query.getStatus())) {
            wrapper.eq(KoDispatcher::getStatus, query.getStatus());
        }
        if (Objects.nonNull(query.getBeginTime())) {
            wrapper.ge(KoDispatcher::getCreateTime, DateUtil.formatDate(query.getBeginTime()));
        }
        if (Objects.nonNull(query.getEndTime())) {
            wrapper.le(KoDispatcher::getCreateTime, DateUtil.formatDate(DateUtil.offsetDay(query.getEndTime(), 1)));
        }
        wrapper.eq(KoDispatcher::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex());
        return list(wrapper);
    }

    @Override
    public List<KoDispatcher> getPageList(KoDispatcherQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }
}
