package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.ivr.domain.entity.FlowNodes;
import com.och.ivr.mapper.FlowNodesMapper;
import com.och.ivr.service.IFlowNodesService;
import com.och.security.utils.SecurityUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Override
    public void addByFlowId(List<FlowNodes> nodes, Long flowId) {
        if(CollectionUtils.isNotEmpty(nodes)){
            nodes.forEach(node -> node.setFlowId(flowId));
            saveBatch(nodes);
        }

    }

    @Override
    public void editByFlowId(List<FlowNodes> nodes, Long flowId) {
        if(CollectionUtils.isEmpty(nodes)){
            return;
        }
        deleteByFlowId(flowId);
        addByFlowId(nodes, flowId);
    }

    @Override
    public void deleteByFlowId(Long flowId) {
        update(new LambdaUpdateWrapper<FlowNodes>()
                .set(FlowNodes::getUpdateBy, SecurityUtils.getUserId())
                .set(FlowNodes::getUpdateTime, new Date())
                .set(FlowNodes::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()).eq(FlowNodes::getFlowId, flowId));
    }
}

