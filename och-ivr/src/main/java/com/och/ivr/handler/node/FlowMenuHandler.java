package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.properties.FlowMenuNodeProperties;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 菜单节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowMenuHandler extends AbstractIFlowNodeHandler {


    public FlowMenuHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
    }

    @Override
    public void execute(FlowDataContext flowData) {
        FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
        if (Objects.isNull(flowNode)) {
            throw new FlowNodeException("节点配置错误");
        }
        FlowMenuNodeProperties flowMenuNodeProperties = JSONObject.parseObject(flowNode.getProperties(), FlowMenuNodeProperties.class);
        if (Objects.isNull(flowMenuNodeProperties)) {
            throw new FlowNodeException("节点配置条件错误");
        }
        if (flowMenuNodeProperties.getInterrupt()) {
            //todo 支持中断
        }
        if (flowMenuNodeProperties.getPlaybackType() == 1) {
            fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), flowMenuNodeProperties.getFile());
        } else if (flowMenuNodeProperties.getPlaybackType() == 2) {
            //tts 播放
        }
        //todo 收号

        if (flowMenuNodeProperties.getNotPlaybackType() == 1) {
            fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), flowMenuNodeProperties.getNotFile());
        } else if (flowMenuNodeProperties.getNotPlaybackType() == 2) {
            //tts 播放
        }
        if (flowMenuNodeProperties.getErrorPlaybackType() == 1) {
            fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), flowMenuNodeProperties.getErrorFile());
        } else if (flowMenuNodeProperties.getErrorPlaybackType() == 2) {
            //tts 播放
        }

    }
}
