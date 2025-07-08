package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.query.CallTaskAddQuery;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.calltask.service.IPredictiveDialerService;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.calltask.mapper.CallTaskMapper;
import com.och.calltask.domain.entity.CallTask;
import com.och.calltask.service.ICallTaskService;
import com.och.common.enums.CallTaskStatusEnum;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.ivr.service.IFlowInfoService;
import com.och.system.domain.entity.CallDisplayPool;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.vo.agent.SipSimpleAgent;
import com.och.system.service.ICallDisplayPoolService;
import com.och.system.service.ICallSkillService;
import com.och.system.service.ISysUserService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
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
 * @since 2025-07-08 10:42:38
 */
@RequiredArgsConstructor
@Service
public class CallTaskServiceImpl extends BaseServiceImpl<CallTaskMapper, CallTask> implements ICallTaskService {

    private IPredictiveDialerService predictiveDialerService;
    private ISysUserService sysUserService;
    private final ICallDisplayPoolService callDisplayPoolService;
    private final ICallSkillService callSkillService;
    private final IFlowInfoService flowInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(CallTaskAddQuery query) {
        Boolean isExist =checkName(query.getName());
        if(isExist){
            throw new CommonException("任务名称已存在");
        }
        CallTask task = new CallTask();
        task.setName(query.getName());
        task.setType(query.getType());
        task.setPriority(query.getPriority());
        task.setStartDay(query.getStartDay());
        task.setEndDay(query.getEndDay());
        task.setSTime(query.getSTime());
        task.setETime(query.getETime());
        task.setWorkCycle(query.getWorkCycle());
        task.setStatus(CallTaskStatusEnum.NOT_START.getCode());
        task.setCrowdId(query.getCrowdId());
        if (query.getType().equals(1) && Objects.isNull(query.getAssignmentType())){
            throw new CommonException("预览任务必须指定分配方式");
        }
        task.setAssignmentType(query.getAssignmentType());
        task.setIsPriority(query.getIsPriority());
        task.setReceiveLimit(query.getReceiveLimit());
        task.setAgentList(JSONObject.toJSONString(query.getAgentList()));
        task.setCompleteType(query.getCompleteType());
        task.setPhonePoolId(query.getPhonePoolId());

        if(query.getType() == 0){
            if(Objects.isNull(query.getTransferType())){
                throw new CommonException("预测任务必须指定转接方式");
            }
            if(Objects.isNull(query.getTransferValue())){
                throw new CommonException("预测任务必须指定转接方式值");
            }
        }
        task.setTransferType(query.getTransferType());
        task.setTransferValue(query.getTransferValue());
        task.setRecall(query.getRecall());
        task.setRecallNum(query.getRecallNum());
        task.setRecallTime(query.getRecallTime());
        task.setRemark(query.getRemark());
        if(save(task)){
            predictiveDialerService.createTask(task.getId(),query.getStartDay(),query.getEndDay(),query.getSTime(),query.getETime(),query.getWorkCycle());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(CallTaskAddQuery query) {
        CallTask callTask = getById(query.getId());
        if(Objects.isNull(callTask)){
            throw new CommonException("无效ID");
        }
        if(!Objects.equals(callTask.getName(), query.getName())){
            Boolean isExist =checkName(query.getName());
            if(isExist){
                throw new CommonException("任务名称已存在");
            }
        }
        CallTask task = new CallTask();
        task.setId(query.getId());
        task.setName(query.getName());
        task.setType(query.getType());
        task.setPriority(query.getPriority());
        task.setStartDay(query.getStartDay());
        task.setEndDay(query.getEndDay());
        task.setSTime(query.getSTime());
        task.setETime(query.getETime());
        task.setWorkCycle(query.getWorkCycle());
        task.setStatus(CallTaskStatusEnum.NOT_START.getCode());
        task.setCrowdId(query.getCrowdId());
        if (query.getType().equals(1) && Objects.isNull(query.getAssignmentType())){
            throw new CommonException("预览任务必须指定分配方式");
        }
        task.setAssignmentType(query.getAssignmentType());
        task.setIsPriority(query.getIsPriority());
        task.setReceiveLimit(query.getReceiveLimit());
        task.setAgentList(JSONObject.toJSONString(query.getAgentList()));
        task.setCompleteType(query.getCompleteType());
        task.setPhonePoolId(query.getPhonePoolId());
        if(query.getType() == 0){
            if(Objects.isNull(query.getTransferType())){
                throw new CommonException("预测任务必须指定转接方式");
            }
            if(Objects.isNull(query.getTransferValue())){
                throw new CommonException("预测任务必须指定转接方式值");
            }
        }
        task.setTransferType(query.getTransferType());
        task.setTransferValue(query.getTransferValue());
        task.setRecall(query.getRecall());
        task.setRecallNum(query.getRecallNum());
        task.setRecallTime(query.getRecallTime());
        task.setRemark(query.getRemark());
        if(updateById(task)){
            predictiveDialerService.createTask(task.getId(),query.getStartDay(),query.getEndDay(),query.getSTime(),query.getETime(),query.getWorkCycle());
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
        BeanUtils.copyProperties(task, taskVo, "agentList");
        taskVo.setAgentList(JSONArray.parseArray(task.getAgentList(), SipSimpleAgent.class));
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
        sysUserService.decorate(list);
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
    public Boolean updateStatus(Long id, CallTaskStatusEnum callTaskStatusEnum) {
        CallTask task = new CallTask();
        task.setId(id);
        task.setStatus(callTaskStatusEnum.getCode());
        return updateById(task);
    }

    private Boolean checkName(String name) {
        long count = count(new LambdaQueryWrapper<CallTask>().eq(CallTask::getName, name).eq(BaseEntity::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        return count > 0;
    }
}

