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
import com.och.system.domain.CallEngine;
import com.och.system.service.ICallEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * 开始节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@Component("FlowStartHandler")
public class FlowStartHandler extends AbstractIFlowNodeHandler {

    @Autowired
    private ICallEngineService iCallEngineService;

    public FlowStartHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService, ICallEngineService iCallEngineService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
        this.iCallEngineService = iCallEngineService;
    }

    @Override
    public void execute(FlowDataContext flowData) throws FlowNodeException {
        try {
            FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
            String properties = flowNode.getProperties();
            if (StringUtils.isNotBlank(properties)) {
                FlowStartNodeProperties startNodeProperties = JSONObject.parseObject(properties, FlowStartNodeProperties.class);
                if(Objects.nonNull(startNodeProperties.getAsrEngine())){
                    CallEngine asrEngine = iCallEngineService.getDetail(startNodeProperties.getAsrEngine());
                    if(Objects.nonNull(asrEngine)){
                        flowData.setAsrEngine(asrEngine.getProfile());
                    }
                }
                if(Objects.nonNull(startNodeProperties.getTtsEngine())){
                    CallEngine ttsEngine = iCallEngineService.getDetail(startNodeProperties.getTtsEngine());
                    if(Objects.nonNull(ttsEngine)){
                        flowData.setTtsEngine(ttsEngine.getProfile());
                        flowData.setTtsVoice(ttsEngine.getTimbre());
                    }
                }
            }
            iFlowNoticeService.notice(2, "next", flowData);
        } catch (Exception e) {
            log.error("开始节点处理异常", e);
            throw new FlowNodeException(e);
        }
    }
}
