package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.handler.route.FlowAgentRouteHandler;
import com.och.ivr.handler.route.FlowCallOutRouteHandler;
import com.och.ivr.handler.route.FlowSipRouteHandler;
import com.och.ivr.handler.route.FlowSkillGroupRouteHandler;
import com.och.ivr.properties.FlowNodeProperties;
import com.och.ivr.properties.FlowTransferNodeProperties;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 转接节点处理
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@Component("FlowTransferHandler")
public class FlowTransferHandler extends AbstractIFlowNodeHandler{

    private final FlowAgentRouteHandler flowAgentRouteHandler;
    private final FlowCallOutRouteHandler flowCallOutRouteHandler;
    private final FlowSipRouteHandler flowSipRouteHandler;
    private final FlowSkillGroupRouteHandler flowSkillGroupRouteHandler;

    public FlowTransferHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService, FlowAgentRouteHandler flowAgentRouteHandler, FlowCallOutRouteHandler flowCallOutRouteHandler, FlowSipRouteHandler flowSipRouteHandler, FlowSkillGroupRouteHandler flowSkillGroupRouteHandler) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
        this.flowAgentRouteHandler = flowAgentRouteHandler;
        this.flowCallOutRouteHandler = flowCallOutRouteHandler;
        this.flowSipRouteHandler = flowSipRouteHandler;
        this.flowSkillGroupRouteHandler = flowSkillGroupRouteHandler;
    }


    @Override
    public void execute(FlowDataContext flowData) {
        FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
        FlowTransferNodeProperties transferNodeProperties = JSONObject.parseObject(flowNode.getProperties(), FlowTransferNodeProperties.class);
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
    }

    @Override
    public void businessHandler(String event, FlowDataContext flowData) throws FlowNodeException {

    }
}
