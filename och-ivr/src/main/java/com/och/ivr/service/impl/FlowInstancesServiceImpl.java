package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.ivr.domain.entity.FlowNodes;
import com.och.ivr.mapper.FlowInstancesMapper;
import com.och.ivr.domain.entity.FlowInstances;
import com.och.ivr.service.IFlowInstancesService;
import org.springframework.stereotype.Service;

/**
 * 存储流程实例的基本信息(FlowInstances)表服务实现类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Service
public class FlowInstancesServiceImpl extends BaseServiceImpl<FlowInstancesMapper, FlowInstances> implements IFlowInstancesService {


    // 获取流程实例
    public FlowInstances getFlowInstance(String instanceId) {
       return getOne(new LambdaQueryWrapper<FlowInstances>().eq(FlowInstances::getId, instanceId).eq(BaseEntity::getDelFlag,0).last("limit 1"));
    }

    // 更新当前节点
    public void updateCurrentNode(FlowInstances instance, FlowNodes nextNode) {
        instance.setCurrentNodeId(nextNode.getId());
        updateById(instance);
    }

    // 标记流程为已完成
    public void markProcessCompleted(FlowInstances instance) {
        instance.setStatus(2);
        updateById(instance);
    }

    // 标记流程为失败
    public void markProcessFailed(FlowInstances instance) {
        instance.setStatus(3);
        updateById(instance);
    }
}

