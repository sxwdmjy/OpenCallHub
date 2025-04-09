package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestRequestType;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import com.och.common.domain.CallInfo;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.properties.FlowHttpNodeProperties;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * HTTP节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */

@Slf4j
@Component("FlowHttpHandler")
public class FlowHttpHandler extends AbstractIFlowNodeHandler {

    public FlowHttpHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
    }

    @Override
    public void execute(FlowDataContext flowData) throws FlowNodeException {
        log.info("HTTP节点处理 flowData：{}", flowData);
        FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
        if (Objects.isNull(flowNode)) {
            throw new FlowNodeException("节点配置错误");
        }
        FlowHttpNodeProperties flowHttpNodeProperties = JSONObject.parseObject(flowNode.getProperties(), FlowHttpNodeProperties.class);
        if (Objects.isNull(flowHttpNodeProperties)) {
            throw new FlowNodeException("节点配置条件错误");
        }
        CallInfo callInfo = fsCallCacheService.getCallInfo(flowData.getCallId());
        if (Objects.isNull(callInfo)) {
            throw new FlowNodeException("callInfo is null");
        }
        if(StringUtils.isEmpty(flowHttpNodeProperties.getUrl())){
            throw new FlowNodeException("url is null");
        }
        if(Objects.isNull(flowHttpNodeProperties.getMethod())){
            throw new FlowNodeException("method is null");
        }
        ForestRequest<JSONObject> request = Forest.request(JSONObject.class);
        request.url();
        switch (flowHttpNodeProperties.getMethod()){
            case 1 -> {
                request.setType(ForestRequestType.GET);
                request.addQuery(JSONObject.parseObject(flowHttpNodeProperties.getParams()));
            }
            case 2 -> {
                request.setType(ForestRequestType.POST);
                request.addBody(JSONObject.parseObject(flowHttpNodeProperties.getParams()));
            }
            case 3 -> {
                request.setType(ForestRequestType.PUT);
                request.addBody(JSONObject.parseObject(flowHttpNodeProperties.getParams()));
            }
            case 4 -> {
                request.setType(ForestRequestType.DELETE);
                request.addQuery(JSONObject.parseObject(flowHttpNodeProperties.getParams()));
            }
        }
        if(CollectionUtils.isNotEmpty(flowHttpNodeProperties.getHeaders())){
            for (FlowHttpNodeProperties.HttpHeader header : flowHttpNodeProperties.getHeaders()) {
                request.addHeader(header.getName(), header.getValue());
            }
        }
        JSONObject result = request.executeAsFuture().getResponse().getResult();
        if (Objects.isNull(result)) {
            throw new FlowNodeException("HTTP请求失败");
        }
        if (StringUtils.isNotEmpty(flowHttpNodeProperties.getResult())) {
            flowData.setHttpResult(result.getString(flowHttpNodeProperties.getResult()));
            callInfo.setFlowDataContext(flowData);
            fsCallCacheService.saveCallInfo(callInfo);
        }
        iFlowNoticeService.notice(2, "next", flowData);

    }
}
