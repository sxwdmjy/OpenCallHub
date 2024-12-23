package com.och.ivr.service;

import com.och.common.base.IBaseService;
import com.och.ivr.domain.entity.FlowNodes;

import java.util.List;

/**
 * 存储流程中的节点信息(FlowNodes)表服务接口
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
public interface IFlowNodesService extends IBaseService<FlowNodes> {

    List<FlowNodes> findByFlowId(String flowId);

    void addByFlowId(List<FlowNodes> nodes, Long flowId);

    void editByFlowId(List<FlowNodes> nodes, Long flowId);

    void deleteByFlowId(Long flowId);
}

