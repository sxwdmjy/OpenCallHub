package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.entity.FlowNodes;
import com.och.ivr.handler.route.FlowAgentRouteHandler;
import com.och.ivr.handler.route.FlowCallOutRouteHandler;
import com.och.ivr.handler.route.FlowSipRouteHandler;
import com.och.ivr.handler.route.FlowSkillGroupRouteHandler;
import com.och.ivr.properties.FlowNodeProperties;
import com.och.ivr.properties.FlowTransferNodeProperties;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.ivr.service.IFlowNodesService;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 转接节点处理
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowTransferHandler extends AbstractIFlowNodeHandler{

    private final FlowAgentRouteHandler flowAgentRouteHandler;
    private final FlowCallOutRouteHandler flowCallOutRouteHandler;
    private final FlowSipRouteHandler flowSipRouteHandler;
    private final FlowSkillGroupRouteHandler flowSkillGroupRouteHandler;

    public FlowTransferHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowNodesService iFlowNodesService, IFlowEdgesService iFlowEdgesService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, FlowAgentRouteHandler flowAgentRouteHandler, FlowCallOutRouteHandler flowCallOutRouteHandler, FlowSipRouteHandler flowSipRouteHandler, FlowSkillGroupRouteHandler flowSkillGroupRouteHandler) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowNodesService, iFlowEdgesService, iFlowInfoService, iFlowInstancesService, fsClient);
        this.flowAgentRouteHandler = flowAgentRouteHandler;
        this.flowCallOutRouteHandler = flowCallOutRouteHandler;
        this.flowSipRouteHandler = flowSipRouteHandler;
        this.flowSkillGroupRouteHandler = flowSkillGroupRouteHandler;
    }

    @Override
    public void execute(FlowDataContext flowData) {
        Long currentNodeId = flowData.getCurrentNodeId();
        FlowNodes flowNodes = iFlowNodesService.getById(currentNodeId);
        FlowNodeProperties transferNodeProperties = JSONObject.parseObject(flowNodes.getProperties(), FlowTransferNodeProperties.class);
        if(Objects.isNull(transferNodeProperties)){
            throw new FlowNodeException("转接节点配置错误");
        }
        if(StringUtils.isBlank(transferNodeProperties.getRouteValue())){
            throw new FlowNodeException("转接节点配置错误");
        }

        switch (transferNodeProperties.getRouteType()){
            case 1 ->  flowAgentRouteHandler.handler(flowData,transferNodeProperties);
            case 2 -> flowCallOutRouteHandler.handler(flowData,transferNodeProperties);
            case 3 -> flowSipRouteHandler.handler(flowData,transferNodeProperties);
            case 4 -> flowSkillGroupRouteHandler.handler(flowData,transferNodeProperties);
            default -> throw new FlowNodeException("转接节点配置错误");
        }
        Long nextNodeId = getNextNodeId(flowData, "success");
        iFlowNoticeService.notice(2, ""+nextNodeId, flowData);

    }
}
