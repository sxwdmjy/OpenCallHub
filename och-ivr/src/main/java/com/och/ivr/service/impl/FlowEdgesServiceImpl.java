package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.ivr.domain.FlowEdges;
import com.och.ivr.domain.FlowNodes;
import com.och.ivr.mapper.FlowEdgesMapper;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowNodesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储流程中节点之间的连接信息（流转规则）(FlowEdges)表服务实现类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Service
public class FlowEdgesServiceImpl extends BaseServiceImpl<FlowEdgesMapper, FlowEdges> implements IFlowEdgesService {


    @Autowired
    private IFlowEdgesService flowEdgesService;
    @Autowired
    private IFlowNodesService flowNodesService;

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
    public List<FlowEdges> findBySourceNodeId(String currentNodeId) {
        return list(new LambdaQueryWrapper<FlowEdges>().eq(FlowEdges::getSourceNodeId, currentNodeId));
    }
}

