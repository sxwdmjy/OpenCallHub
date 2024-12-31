package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.query.schedule.CallScheduleAddQuery;
import com.och.system.domain.query.schedule.CallScheduleQuery;
import com.och.system.mapper.CallScheduleMapper;
import com.och.system.service.ICallScheduleService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 日程安排表(CallSchedule)表服务实现类
 *
 * @author danmo
 * @since 2024-08-09 17:14:31
 */
@Service
public class CallScheduleServiceImpl extends BaseServiceImpl<CallScheduleMapper, CallSchedule> implements ICallScheduleService {

    @Override
    public void add(CallScheduleAddQuery query) {
        CallSchedule schedule = new CallSchedule();
        schedule.setQuery2Entity(query);
        save(schedule);
    }

    @Override
    public void update(CallScheduleAddQuery query) {
        CallSchedule schedule = new CallSchedule();
        schedule.setQuery2Entity(query);
        updateById(schedule);
    }

    @Override
    public void delete(CallScheduleQuery query) {
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
        List<CallSchedule> list = ids.stream().map(id -> {
            CallSchedule schedule = new CallSchedule();
            schedule.setId(id);
            schedule.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return schedule;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public CallSchedule getDetail(Long id) {
        return getById(id);
    }

    @Override
    public List<CallSchedule> getList(CallScheduleQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public List<CallSchedule> getPageList(CallScheduleQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }
}

