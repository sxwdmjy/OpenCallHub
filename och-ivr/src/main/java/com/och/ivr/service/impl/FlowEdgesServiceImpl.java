package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.ivr.domain.entity.FlowEdges;
import com.och.ivr.domain.entity.FlowNodes;
import com.och.ivr.mapper.FlowEdgesMapper;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowNodesService;
import com.och.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 存储流程中节点之间的连接信息（流转规则）(FlowEdges)表服务实现类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@RequiredArgsConstructor
@Service
public class FlowEdgesServiceImpl extends BaseServiceImpl<FlowEdgesMapper, FlowEdges> implements IFlowEdgesService {


    private final IFlowEdgesService flowEdgesService;
    private final IFlowNodesService flowNodesService;

    // 获取当前节点的后继节点
    public List<FlowNodes> getNextNodes(String currentNodeId) {
        List<FlowEdges> edges = flowEdgesService.findBySourceNodeId(currentNodeId);
        List<FlowNodes> nextNodes = new ArrayList<>();
        for (FlowEdges edge : edges) {
            FlowNodes nextNode = flowNodesService.getById(edge.getTargetNodeId());
            if (nextNode != null) {
                nextNodes.add(nextNode);
            }
        }
        return nextNodes;
    }

    @Override
    public void addByFlowId(List<FlowEdges> edges, Long flowId) {
        if(CollectionUtils.isNotEmpty(edges)){
            edges.forEach(item->item.setFlowId(flowId));
            saveBatch(edges);
        }
    }

    @Override
    public void editByFlowId(List<FlowEdges> edges, Long flowId) {
        if(CollectionUtils.isEmpty(edges)){
            return;
        }
        deleteByFlowId(flowId);
        addByFlowId(edges, flowId);
    }

    @Override
    public void deleteByFlowId(Long flowId) {
        update(new LambdaUpdateWrapper<FlowEdges>().set(FlowEdges::getUpdateBy, SecurityUtils.getUserId()).set(FlowEdges::getUpdateTime, new Date()).set(FlowEdges::getDelFlag, DeleteStatusEnum.DELETE_YES.getIndex()).eq(FlowEdges::getFlowId, flowId));
    }

    @Override
    public List<FlowEdges> findBySourceNodeId(String currentNodeId) {
        return list(new LambdaQueryWrapper<FlowEdges>().eq(FlowEdges::getSourceNodeId, currentNodeId));
    }
}

