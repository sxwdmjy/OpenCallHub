package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.calltask.domain.entity.OchDataSources;
import com.och.calltask.domain.query.CallTaskAddQuery;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.calltask.service.IOchDataSourcesService;
import com.och.common.base.BaseServiceImpl;
import com.och.calltask.mapper.CallTaskMapper;
import com.och.calltask.domain.entity.CallTask;
import com.och.calltask.service.ICallTaskService;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.ivr.service.IFlowInfoService;
import com.och.system.domain.entity.CallDisplayPool;
import com.och.system.domain.entity.CallSkill;
import com.och.system.service.ICallDisplayPoolService;
import com.och.system.service.ICallSkillService;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 外呼任务表(CallTask)表服务实现类
 *
 * @author danmo
 * @since 2025-06-18 15:53:57
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CallTaskServiceImpl extends BaseServiceImpl<CallTaskMapper, CallTask> implements ICallTaskService {

    private final IOchDataSourcesService dataSourcesService;
    private final ICallDisplayPoolService callDisplayPoolService;
    private final ICallSkillService callSkillService;
    private final IFlowInfoService flowInfoService;
    private final ISysUserService iSysUserService;

    @Override
    public void add(CallTaskAddQuery query) {
        CallTask task = new CallTask();
        BeanUtils.copyProperties(query, task);
        save(task);
    }

    @Override
    public void edit(CallTaskAddQuery query) {
        CallTask callTask = getById(query.getId());
        if (Objects.isNull(callTask)) {
            throw new CommonException("无效ID");
        }
        CallTask updateTask = new CallTask();
        updateTask.setId(query.getId());
        BeanUtils.copyProperties(query, updateTask);
        updateById(updateTask);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void detele(CallTaskQuery query) {
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
        List<CallTask> list = ids.stream().map(id -> {
            CallTask callTask = new CallTask();
            callTask.setId(id);
            callTask.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return callTask;
        }).toList();
        updateBatchById(list);
    }

    @Override
    public CallTaskVo getDetail(Long id) {
        CallTask task = getById(id);
        if (Objects.isNull(task)){
            throw new CommonException("无效ID");
        }
        CallTaskVo taskVo = new CallTaskVo();
        BeanUtils.copyProperties(task, taskVo);
        OchDataSources dataSources = dataSourcesService.getById(task.getSourceId());
        if (Objects.nonNull(dataSources)){
            taskVo.setSourceName(dataSources.getName());
        }
        if (Objects.nonNull(task.getPhonePoolId())){
            CallDisplayPool displayPool = callDisplayPoolService.getById(task.getPhonePoolId());
            if (Objects.nonNull(displayPool)){
                taskVo.setPhonePoolName(displayPool.getName());
            }
        }
        if (Objects.nonNull(task.getTransferType())){
            switch (task.getTransferType()){
                case 0 -> {
                    CallSkill callSkill = callSkillService.getById(task.getTransferValue());
                    if (Objects.nonNull(callSkill)){
                        taskVo.setTransferValueName(callSkill.getName());
                    }
                }
                case 1 -> {
                    taskVo.setTransferValueName(flowInfoService.getById(task.getTransferValue()).getName());
                }
                case 2 -> {

                }
                case 3 -> {
                }
            }
        }
        return taskVo;
    }

    @Override
    public List<CallTaskVo> pageList(CallTaskQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }

    @Override
    public List<CallTaskVo> getList(CallTaskQuery query) {
        return this.baseMapper.getList(query);
    }


}

