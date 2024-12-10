package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallInPhoneRel;
import com.och.system.domain.query.callin.CallInPhoneRelQuery;
import com.och.system.mapper.CallInPhoneRelMapper;
import com.och.system.service.ICallInPhoneRelService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 呼入号码路由子码表(CallInPhoneRel)表服务实现类
 *
 * @author danmo
 * @since 2024-12-10 10:29:11
 */
@Service
public class CallInPhoneRelServiceImpl extends BaseServiceImpl<CallInPhoneRelMapper, CallInPhoneRel> implements ICallInPhoneRelService {


    @Override
    public void saveByCallInId(Long callInId, List<CallInPhoneRelQuery> routList) {
        if(CollectionUtil.isEmpty(routList)){
            return;
        }
        List<CallInPhoneRel> phoneRels = routList.stream().map(item -> {
            CallInPhoneRel rel = new CallInPhoneRel();
            rel.setCallInId(callInId);
            rel.setScheduleId(item.getScheduleId());
            rel.setRouteType(item.getRouteType());
            rel.setRouteValue(item.getRouteValue());
            return rel;
        }).collect(Collectors.toList());
        saveBatch(phoneRels);
    }

    @Override
    public void updateByCallInId(Long callInId, List<CallInPhoneRelQuery> routList) {
        delByCallInIds(Collections.singletonList(callInId));
        saveByCallInId(callInId,routList);
    }

    @Override
    public void delByCallInIds(List<Long> callInIds) {
        if(CollectionUtil.isEmpty(callInIds)){
            return;
        }
        CallInPhoneRel rel = new CallInPhoneRel();
        rel.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        update(rel,new LambdaQueryWrapper<CallInPhoneRel>().in(CallInPhoneRel::getCallInId,callInIds));
    }
}

