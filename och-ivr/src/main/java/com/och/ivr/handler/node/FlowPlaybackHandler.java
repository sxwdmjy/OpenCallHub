package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.entity.FlowNodes;
import com.och.ivr.properties.FlowPlaybackNodeProperties;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.ivr.service.IFlowNodesService;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 放音节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowPlaybackHandler extends AbstractIFlowNodeHandler {


    public FlowPlaybackHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowNodesService iFlowNodesService, IFlowEdgesService iFlowEdgesService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowNodesService, iFlowEdgesService, iFlowInfoService, iFlowInstancesService, fsClient);
    }

    @Override
    public void execute(FlowDataContext flowData) {
        FlowNodes flowNodes = iFlowNodesService.getById(flowData.getCurrentNodeId());
        if (Objects.isNull(flowNodes)){
            throw new FlowNodeException("节点配置错误");
        }
        String properties = flowNodes.getProperties();
        if (StringUtils.isNotBlank(properties)){
            FlowPlaybackNodeProperties playbackNodeProperties = JSONObject.parseObject(properties, FlowPlaybackNodeProperties.class);
            if (playbackNodeProperties.getInterrupt()){
               // fsClient.detectSpeechResume(flowData.getAddress(), flowData.getCallId(), flowData.getUniqueId());
            }
            if (playbackNodeProperties.getPlaybackType() == 1){
                fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), playbackNodeProperties.getFile());
            }else if (playbackNodeProperties.getPlaybackType() == 2){
                //tts 播放

            }
        }
        Long nextNodeId = getNextNodeId(flowData, "success");
        iFlowNoticeService.notice(2, ""+nextNodeId, flowData);
    }
}
