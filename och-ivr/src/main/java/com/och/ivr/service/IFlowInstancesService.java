package com.och.ivr.service;

import com.och.common.base.IBaseService;
import com.och.ivr.domain.entity.FlowInstances;

/**
 * 存储流程实例的基本信息(FlowInstances)表服务接口
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
public interface IFlowInstancesService extends IBaseService<FlowInstances> {

    FlowInstances getFlowInstance(String processInstanceId);

    void updateCurrentNode(FlowInstances instance, String nextNode);

    void markProcessCompleted(FlowInstances instance);

    void markProcessFailed(FlowInstances instance);
}

