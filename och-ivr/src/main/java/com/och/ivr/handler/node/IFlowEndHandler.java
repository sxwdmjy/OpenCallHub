package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.entity.FlowNodes;
import com.och.ivr.properties.FlowEndNodeProperties;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.ivr.service.IFlowNodesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 结束节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@Component
public class IFlowEndHandler extends AbstractIFlowNodeHandler {


    public IFlowEndHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowNodesService iFlowNodesService, IFlowEdgesService iFlowEdgesService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowNodesService, iFlowEdgesService, iFlowInfoService, iFlowInstancesService, fsClient);
    }

    @Override
    public void execute(FlowDataContext flowData) {
        log.info("结束节点处理 flowData：{}", flowData);
        FlowNodes flowNodes = iFlowNodesService.getById(flowData.getCurrentNodeId());
        if(Objects.isNull(flowNodes)){
            throw new FlowNodeException("结束节点配置错误");
        }
        String properties = flowNodes.getProperties();
        if(StringUtils.isNotBlank(properties)){
            FlowEndNodeProperties endNodeProperties = JSONObject.parseObject(properties, FlowEndNodeProperties.class);
            //判断是否挂机
            if (endNodeProperties.getHangUp()){
                fsClient.hangupCall(flowData.getAddress(), flowData.getCallId(), flowData.getUniqueId());
            }
        }

    }
}
