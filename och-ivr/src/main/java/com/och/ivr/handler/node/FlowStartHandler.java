package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import com.och.common.domain.CallInfo;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.properties.FlowStartNodeProperties;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
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


    public FlowStartHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
    }

    @Override
    public void execute(FlowDataContext flowData) throws FlowNodeException {
        try {
            FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
            String properties = flowNode.getProperties();
            CallInfo callInfo = fsCallCacheService.getCallInfo(flowData.getCallId());
            if(StringUtils.isNotBlank(properties)){
                FlowStartNodeProperties startNodeProperties = JSONObject.parseObject(properties, FlowStartNodeProperties.class);
                callInfo.setAsrEngine(startNodeProperties.getAsrEngine());
                callInfo.setTtsEngine(startNodeProperties.getTtsEngine());
            }
            fsCallCacheService.saveCallInfo(callInfo);
            iFlowNoticeService.notice(2, "next", flowData);
        } catch (Exception e) {
            throw new FlowNodeException(e);
        }
    }
}
