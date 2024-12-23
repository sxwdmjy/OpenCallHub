package com.och.ivr.service;

import com.och.common.base.IBaseService;
import com.och.ivr.domain.entity.FlowEdges;
import com.och.ivr.domain.entity.FlowNodes;

import java.util.List;

/**
 * 存储流程中节点之间的连接信息（流转规则）(FlowEdges)表服务接口
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
public interface IFlowEdgesService extends IBaseService<FlowEdges> {

    List<FlowEdges> findBySourceNodeId(String currentNodeId);

    List<FlowNodes> getNextNodes(String id);

    void addByFlowId(List<FlowEdges> edges, Long flowId);

    void editByFlowId(List<FlowEdges> edges, Long flowId);
    void deleteByFlowId(Long flowId);
}

