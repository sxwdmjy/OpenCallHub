package com.och.ivr.handler;

import com.alibaba.fastjson.JSONObject;
import com.och.common.constant.FlowDataContext;
import com.och.common.domain.CallInfo;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.entity.FlowNodes;
import com.och.ivr.properties.FlowStartNodeProperties;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.ivr.service.IFlowNodesService;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 开始节点处理
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowStartHandler extends AbstractIFlowNodeHandler{


    public FlowStartHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowNodesService iFlowNodesService, IFlowEdgesService iFlowEdgesService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowNodesService, iFlowEdgesService, iFlowInfoService, iFlowInstancesService, fsClient);
    }

    @Override
    public void execute(FlowDataContext flowData) throws FlowNodeException {
        try {
            FlowNodes startNode = iFlowNodesService.getById(flowData.getCurrentNodeId());
            String properties = startNode.getProperties();
            CallInfo callInfo = fsCallCacheService.getCallInfo(flowData.getCallId());
            if(StringUtils.isNotBlank(properties)){
                FlowStartNodeProperties startNodeProperties = JSONObject.parseObject(properties, FlowStartNodeProperties.class);
                callInfo.setAsrEngine(startNodeProperties.getAsrEngine());
                callInfo.setTtsEngine(startNodeProperties.getTtsEngine());
            }
            Long nextNodeId = getNextNodeId(flowData, "");
            if(Objects.isNull(nextNodeId)){
                throw new FlowNodeException("开始节点未找到下一节点");
            }
            callInfo.setNextNodeId(nextNodeId);
            fsCallCacheService.saveCallInfo(callInfo);
        } catch (Exception e) {
            throw new FlowNodeException(e);
        }
    }
}
