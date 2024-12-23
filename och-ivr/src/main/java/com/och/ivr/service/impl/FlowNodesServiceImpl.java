package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.ivr.domain.FlowNodes;
import com.och.ivr.mapper.FlowNodesMapper;
import com.och.ivr.service.IFlowNodesService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 存储流程中的节点信息(FlowNodes)表服务实现类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Service
public class FlowNodesServiceImpl extends BaseServiceImpl<FlowNodesMapper, FlowNodes> implements IFlowNodesService {

    @Override
    public List<FlowNodes> findByFlowId(String flowId) {
        return list(new LambdaQueryWrapper<FlowNodes>().eq(FlowNodes::getFlowId, flowId).eq(BaseEntity::getDelFlag, 0));
    }
}

