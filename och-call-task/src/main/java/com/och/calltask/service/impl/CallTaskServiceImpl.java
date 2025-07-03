package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.calltask.domain.entity.CallTask;
import com.och.calltask.domain.query.CallTaskAddQuery;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.calltask.mapper.CallTaskMapper;
import com.och.calltask.service.ICallTaskService;
import com.och.calltask.service.IPredictiveDialerService;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.CallTaskStatusEnum;
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

    private final ICallDisplayPoolService callDisplayPoolService;
    private final ICallSkillService callSkillService;
    private final IFlowInfoService flowInfoService;
    private final ISysUserService iSysUserService;
    private final IPredictiveDialerService predictiveDialerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(CallTaskAddQuery query) {
        CallTask task = new CallTask();
        BeanUtils.copyProperties(query, task);
        if (save(task)) {
            predictiveDialerService.createTask(task.getId(), task.getStartDay(), task.getEndDay(), task.getSTime(), task.getETime(), task.getWorkCycle());
        }
        ;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(CallTaskAddQuery query) {
        CallTask callTask = getById(query.getId());
        if (Objects.isNull(callTask)) {
            throw new CommonException("无效ID");
        }
        CallTask updateTask = new CallTask();
        updateTask.setId(query.getId());
        BeanUtils.copyProperties(query, updateTask);
        if (updateById(updateTask)) {
            predictiveDialerService.createTask(updateTask.getId(), updateTask.getStartDay(), updateTask.getEndDay(), updateTask.getSTime(), updateTask.getETime(), updateTask.getWorkCycle());
        }
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
        if (updateBatchById(list)) {
            predictiveDialerService.deleteTask(ids);
        }
    }

    @Override
    public CallTaskVo getDetail(Long id) {
        CallTask task = getById(id);
        if (Objects.isNull(task)) {
            throw new CommonException("无效ID");
        }
        CallTaskVo taskVo = new CallTaskVo();
        BeanUtils.copyProperties(task, taskVo);
        if (Objects.nonNull(task.getPhonePoolId())) {
            CallDisplayPool displayPool = callDisplayPoolService.getById(task.getPhonePoolId());
            if (Objects.nonNull(displayPool)) {
                taskVo.setPhonePoolName(displayPool.getName());
            }
        }
        if (Objects.nonNull(task.getTransferType())) {
            switch (task.getTransferType()) {
                case 0 -> {
                    CallSkill callSkill = callSkillService.getById(task.getTransferValue());
                    if (Objects.nonNull(callSkill)) {
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
        List<CallTaskVo> list = getList(query);
        iSysUserService.decorate(list);
        return list;
    }

    @Override
    public List<CallTaskVo> getList(CallTaskQuery query) {
        return this.baseMapper.getList(query);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pauseTask(Long id) {
        CallTask task = getById(id);
        if (Objects.isNull(task)) {
            throw new CommonException("无效ID");
        }
        if (Objects.equals(task.getStatus(), CallTaskStatusEnum.PAUSE.getCode())) {
            throw new CommonException("任务已暂停");
        }
        if (Objects.equals(task.getStatus(), CallTaskStatusEnum.NOT_START.getCode())) {
            throw new CommonException("任务未开始");
        }
        if (Objects.equals(task.getStatus(), CallTaskStatusEnum.END.getCode())) {
            throw new CommonException("任务已结束");
        }
        Boolean updated = updateStatus(id, CallTaskStatusEnum.PAUSE);
        if (updated) {
            predictiveDialerService.pauseTask(id);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void startTask(Long id) {
        CallTask task = getById(id);
        if (Objects.isNull(task)) {
            throw new CommonException("无效ID");
        }
        if (Objects.equals(task.getStatus(), CallTaskStatusEnum.PROCESSING.getCode())) {
            throw new CommonException("任务已开始");
        }
        if (Objects.equals(task.getStatus(), CallTaskStatusEnum.END.getCode())) {
            throw new CommonException("任务已结束");
        }
        Boolean updated = updateStatus(id, CallTaskStatusEnum.PROCESSING);
        if (updated) {
            predictiveDialerService.resumeTask(id);
        }
    }

    @Override
    public void endTask(Long id) {
        CallTask task = getById(id);
        if (Objects.isNull(task)) {
            throw new CommonException("无效ID");
        }
        if (Objects.equals(task.getStatus(), CallTaskStatusEnum.END.getCode())) {
            throw new CommonException("任务已结束");
        }
        if (Objects.equals(task.getStatus(), CallTaskStatusEnum.NOT_START.getCode())) {
            throw new CommonException("任务未开始");
        }
        Boolean updated = updateStatus(id, CallTaskStatusEnum.END);
        if (updated) {
            predictiveDialerService.deleteTask(id);
        }
    }

    @Override
    public Boolean updateStatus(Long id, CallTaskStatusEnum statusEnum) {
        CallTask updateTask = new CallTask();
        updateTask.setId(id);
        updateTask.setStatus(statusEnum.getCode());
        return updateById(updateTask);
    }

}

