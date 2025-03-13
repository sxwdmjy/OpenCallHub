package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.constant.EslConstant;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.properties.FlowPlaybackNodeProperties;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.system.domain.entity.SysFile;
import com.och.system.service.ISysFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 放音节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@Component
public class FlowPlaybackHandler extends AbstractIFlowNodeHandler {

    private ISysFileService sysFileService;

    public FlowPlaybackHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
    }

    @Override
    public void execute(FlowDataContext flowData) {
        FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
        if (Objects.isNull(flowNode)){
            throw new FlowNodeException("节点配置错误");
        }
        String properties = flowNode.getProperties();
        if (StringUtils.isNotBlank(properties)){
            FlowPlaybackNodeProperties playbackNodeProperties = JSONObject.parseObject(properties, FlowPlaybackNodeProperties.class);
            if (playbackNodeProperties.getInterrupt()){
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.PLAYBACK_TERMINATORS_ANY);

            }else {
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.PLAYBACK_TERMINATORS);
            }
            if (playbackNodeProperties.getPlaybackType() == 1){
                SysFile sysFile = sysFileService.getById(playbackNodeProperties.getFileId());
                fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), sysFile.getFileName());
            }else if (playbackNodeProperties.getPlaybackType() == 2){
                //tts 播放
                String ttsEngine = flowData.getTtsEngine();
                if(StringUtils.isNotBlank(ttsEngine)){
                    String ttsVoice = flowData.getTtsVoice();
                    fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_ENGINE+ ttsEngine);
                    fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_VOICE+ ttsVoice);
                }
                String fileName = "say:" + playbackNodeProperties.getContent() + "'";
                fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), fileName);
            }
        }
        iFlowNoticeService.notice(2, "next", flowData);
    }
}
