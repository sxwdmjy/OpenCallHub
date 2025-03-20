package com.och.ivr.handler.node;

import com.alibaba.fastjson.JSONObject;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.EslConstant;
import com.och.common.constant.FlowDataContext;
import com.och.common.domain.CallInfo;
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
import com.och.system.domain.vo.file.VoiceFileVo;
import com.och.system.service.ISysFileService;
import com.och.system.service.IVoiceFileService;
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
@Component("FlowPlaybackHandler")
public class FlowPlaybackHandler extends AbstractIFlowNodeHandler {

    private final IVoiceFileService iVoiceFileService;

    public FlowPlaybackHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService, IVoiceFileService iVoiceFileService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
        this.iVoiceFileService = iVoiceFileService;
    }

    @Override
    public void execute(FlowDataContext flowData) {
        FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
        if (Objects.isNull(flowNode)){
            throw new FlowNodeException("节点配置错误");
        }
        CallInfo callInfo = fsCallCacheService.getCallInfo(flowData.getCallId());
        if (Objects.isNull(callInfo)){
            throw new FlowNodeException("callInfo is null");
        }
        callInfo.getDetailList().forEach(detail -> {
            if (Objects.equals(detail.getTransferType(), 2)){
                detail.setInstanceId(flowData.getInstanceId());
            }
        });
        callInfo.setDetailList(callInfo.getDetailList());
        callInfo.setFlowDataContext(flowData);
        fsCallCacheService.saveCallInfo(callInfo);
        String properties = flowNode.getProperties();
        if (StringUtils.isNotBlank(properties)){
            FlowPlaybackNodeProperties playbackNodeProperties = JSONObject.parseObject(properties, FlowPlaybackNodeProperties.class);
            if (Objects.nonNull(playbackNodeProperties.getInterrupt()) && playbackNodeProperties.getInterrupt()){
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.PLAYBACK_TERMINATORS_ANY);

            }else {
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.PLAYBACK_TERMINATORS);
            }
            fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.PLAYBACK_DELIMITER);
            if (playbackNodeProperties.getPlaybackType() == 1){
                VoiceFileVo voiceFile = iVoiceFileService.getDetail(playbackNodeProperties.getFileId());
                StringBuilder fileName = new StringBuilder(voiceFile.getFileName());
                if(playbackNodeProperties.getNum() > 1){
                    for (int i = 1; i < playbackNodeProperties.getNum(); i++){
                        fileName.append(EslConstant.EXCLAMATION).append(voiceFile.getFileName());
                    }
                }
                fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), fileName.toString());
            }else if (playbackNodeProperties.getPlaybackType() == 2){
                //tts 播放
                String ttsEngine = flowData.getTtsEngine();
                if(StringUtils.isNotBlank(ttsEngine)){
                    String ttsVoice = flowData.getTtsVoice();
                    fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_ENGINE+ ttsEngine);
                    fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_VOICE+ ttsVoice);
                }
                StringBuilder fileName = new StringBuilder("say:" + playbackNodeProperties.getContent() + "'");
                if (playbackNodeProperties.getNum() > 1){
                    for (int i = 1; i < playbackNodeProperties.getNum(); i++){
                        fileName.append(EslConstant.EXCLAMATION + "say:").append(playbackNodeProperties.getContent()).append("'");
                    }
                }
                fsClient.playFile(flowData.getAddress(), flowData.getUniqueId(), fileName.toString());
            }
        }

    }
}
