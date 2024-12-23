package com.och.ivr.service;

import com.och.common.base.IBaseService;
import com.och.ivr.domain.FlowNodes;

import java.util.List;

/**
 * 存储流程中的节点信息(FlowNodes)表服务接口
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
public interface IFlowNodesService extends IBaseService<FlowNodes> {

    List<FlowNodes> findByFlowId(String flowId);
}

