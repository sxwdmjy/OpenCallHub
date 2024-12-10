package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallInPhone;
import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.query.callin.CallInPhoneAddQuery;
import com.och.system.domain.query.callin.CallInPhoneQuery;
import com.och.system.domain.query.schedule.CallScheduleQuery;
import com.och.system.domain.vo.callin.CallInPhoneRelVo;
import com.och.system.domain.vo.callin.CallInPhoneVo;
import com.och.system.mapper.CallInPhoneMapper;
import com.och.system.service.ICallInPhoneRelService;
import com.och.system.service.ICallInPhoneService;
import com.och.system.service.ICallScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 呼入号码表(CallInPhone)表服务实现类
 *
 * @author danmo
 * @since 2024-12-10 10:29:11
 */
@Service
public class CallInPhoneServiceImpl extends BaseServiceImpl<CallInPhoneMapper, CallInPhone> implements ICallInPhoneService {


    @Autowired
    private ICallInPhoneRelService iCallInPhoneRelService;

    @Autowired
    private ICallScheduleService iCallScheduleService;

    @Override
    public void add(CallInPhoneAddQuery query) {
        CallInPhone callInPhone = new CallInPhone();
        callInPhone.setQuery2Entity(query);
        if (save(callInPhone)) {
            iCallInPhoneRelService.saveByCallInId(callInPhone.getId(), query.getRoutList());
        }
    }

    @Override
    public void update(CallInPhoneAddQuery query) {
        CallInPhone callInPhone = new CallInPhone();
        callInPhone.setQuery2Entity(query);
        if (updateById(callInPhone)) {
            iCallInPhoneRelService.updateByCallInId(callInPhone.getId(), query.getRoutList());
        }
    }

    @Override
    public void delete(CallInPhoneQuery query) {
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
        List<CallInPhone> list = ids.stream().map(id -> {
            CallInPhone callInPhone = new CallInPhone();
            callInPhone.setId(id);
            callInPhone.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return callInPhone;
        }).collect(Collectors.toList());
        if (updateBatchById(list)) {
            iCallInPhoneRelService.delByCallInIds(ids);
        }
    }

    @Override
    public CallInPhoneVo getDetail(CallInPhoneQuery query) {
        return this.baseMapper.getDetail(query);
    }

    @Override
    public List<CallInPhoneVo> getPageList(CallInPhoneQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        List<CallInPhoneVo> list = this.baseMapper.getList(query);
        if (CollectionUtil.isNotEmpty(list)) {
            List<CallInPhoneRelVo> relVoList = list.stream().map(CallInPhoneVo::getRouteList).flatMap(Collection::stream).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(relVoList)) {
                List<Long> scheduleIds = relVoList.stream().map(CallInPhoneRelVo::getScheduleId).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(scheduleIds)) {
                    CallScheduleQuery scheduleQuery = new CallScheduleQuery();
                    scheduleQuery.setIds(scheduleIds);
                    List<CallSchedule> scheduleList = iCallScheduleService.getList(scheduleQuery);
                    if (CollectionUtil.isNotEmpty(scheduleList)) {
                        Map<Long, List<CallSchedule>> scheduleListMap = scheduleList.stream().collect(Collectors.groupingBy(CallSchedule::getId));
                        for (CallInPhoneVo callInPhone : list) {
                            for (CallInPhoneRelVo callInPhoneRel : callInPhone.getRouteList()) {
                                if (scheduleListMap.containsKey(callInPhoneRel.getScheduleId())) {
                                    callInPhoneRel.setScheduleDetail(scheduleListMap.get(callInPhoneRel.getScheduleId()).get(0));
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public CallInPhoneVo getDetailByPhone(String phone) {
        return this.baseMapper.getDetailByPhone(phone);
    }

    @Override
    public List<CallInPhoneVo> getList(CallInPhoneQuery query) {
        List<CallInPhoneVo> list = this.baseMapper.getList(query);
        if (CollectionUtil.isNotEmpty(list)) {
            List<CallInPhoneRelVo> relVoList = list.stream().map(CallInPhoneVo::getRouteList).flatMap(Collection::stream).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(relVoList)) {
                List<Long> scheduleIds = relVoList.stream().map(CallInPhoneRelVo::getScheduleId).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(scheduleIds)) {
                    CallScheduleQuery scheduleQuery = new CallScheduleQuery();
                    scheduleQuery.setIds(scheduleIds);
                    List<CallSchedule> scheduleList = iCallScheduleService.getList(scheduleQuery);
                    if (CollectionUtil.isNotEmpty(scheduleList)) {
                        Map<Long, List<CallSchedule>> scheduleListMap = scheduleList.stream().collect(Collectors.groupingBy(CallSchedule::getId));
                        for (CallInPhoneVo callInPhone : list) {
                            for (CallInPhoneRelVo lfsCallInPhoneRel : callInPhone.getRouteList()) {
                                if (scheduleListMap.containsKey(lfsCallInPhoneRel.getScheduleId())) {
                                    lfsCallInPhoneRel.setScheduleDetail(scheduleListMap.get(lfsCallInPhoneRel.getScheduleId()).get(0));
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}

