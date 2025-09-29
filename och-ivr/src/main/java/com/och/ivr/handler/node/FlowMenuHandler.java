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
import com.och.ivr.properties.FlowMenuNodeProperties;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.system.domain.vo.file.VoiceFileVo;
import com.och.system.service.IVoiceFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 菜单节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@Component("FlowMenuHandler")
public class FlowMenuHandler extends AbstractIFlowNodeHandler {

    private final IVoiceFileService iVoiceFileService;

    public FlowMenuHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService, IVoiceFileService iVoiceFileService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
        this.iVoiceFileService = iVoiceFileService;
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
        CallInfo callInfo = fsCallCacheService.getCallInfo(flowData.getCallId());
        if (Objects.isNull(callInfo)){
            throw new FlowNodeException("callInfo is null");
        }
        callInfo.setFlowDataContext(flowData);
        fsCallCacheService.saveCallInfo(callInfo);
        if (flowMenuNodeProperties.getInterrupt()) {
            fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.PLAYBACK_TERMINATORS_ANY);
        }else {
            fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.PLAYBACK_TERMINATORS);
        }
        String fileName = "";
        String errorFileName = "";
        if (flowMenuNodeProperties.getPlaybackType() == 1) {
            VoiceFileVo voiceFile = iVoiceFileService.getDetail(flowMenuNodeProperties.getFileId());
            fileName = voiceFile.getFileName();
        } else if (flowMenuNodeProperties.getPlaybackType() == 2) {
            //tts 播放
            String ttsEngine = flowData.getTtsEngine();
            if(StringUtils.isNotBlank(ttsEngine)){
                String ttsVoice = flowData.getTtsVoice();
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_ENGINE+ ttsEngine);
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_VOICE+ ttsVoice);
            }
            fileName = "say:" + flowMenuNodeProperties.getContent() + "'";
        }
        if (flowMenuNodeProperties.getErrorPlaybackType() == 1) {
            VoiceFileVo errorFile = iVoiceFileService.getDetail(flowMenuNodeProperties.getErrorFileId());
            errorFileName = errorFile.getFileName();
        } else if (flowMenuNodeProperties.getErrorPlaybackType() == 2) {
            //tts 播放
            String ttsEngine = flowData.getTtsEngine();
            if(StringUtils.isNotBlank(ttsEngine)){
                String ttsVoice = flowData.getTtsVoice();
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_ENGINE+ ttsEngine);
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_VOICE+ ttsVoice);
            }
            errorFileName = "say:" + flowMenuNodeProperties.getErrorContent() + "'";
        }

        if(StringUtils.isBlank(errorFileName)){
            errorFileName = "silence_stream://250";
        }

        //收号
        fsClient.playAndGetDigits(flowData.getAddress(), flowData.getUniqueId(), 1, 1, flowMenuNodeProperties.getMaxRetries(),
                flowMenuNodeProperties.getTimeout(), "#",
                fileName, errorFileName, "MENU_DTMF_RETURN",
                "[*0-9#]+", flowMenuNodeProperties.getTimeout(), null);
    }

    @Override
    public void businessHandler(String event, FlowDataContext flowData) throws FlowNodeException {
        FlowNodeVo flowNode = getFlowNode(flowData.getFlowId(), flowData.getCurrentNodeId());
        if (Objects.isNull(flowNode)) {
            iFlowNoticeService.notice(2, "end", flowData);
        }
        FlowMenuNodeProperties flowMenuNodeProperties = JSONObject.parseObject(flowNode.getProperties(), FlowMenuNodeProperties.class);
        if (Objects.isNull(flowMenuNodeProperties)) {
            iFlowNoticeService.notice(2, "end", flowData);
        }
        String notFileName = "";
        String errorFileName = "";
        if (flowMenuNodeProperties.getNotPlaybackType() == 1) {
            VoiceFileVo notFile = iVoiceFileService.getDetail(flowMenuNodeProperties.getNotFileId());
            notFileName = notFile.getFileName();
        } else if (flowMenuNodeProperties.getNotPlaybackType() == 2) {
            //tts 播放
            String ttsEngine = flowData.getTtsEngine();
            if(StringUtils.isNotBlank(ttsEngine)){
                String ttsVoice = flowData.getTtsVoice();
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_ENGINE+ ttsEngine);
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_VOICE+ ttsVoice);
            }
            notFileName = "say:" + flowMenuNodeProperties.getNotContent() + "'";
        }

        if (flowMenuNodeProperties.getErrorPlaybackType() == 1) {
            VoiceFileVo errorFile = iVoiceFileService.getDetail(flowMenuNodeProperties.getErrorFileId());
            errorFileName = errorFile.getFileName();
        } else if (flowMenuNodeProperties.getErrorPlaybackType() == 2) {
            //tts 播放
            String ttsEngine = flowData.getTtsEngine();
            if(StringUtils.isNotBlank(ttsEngine)){
                String ttsVoice = flowData.getTtsVoice();
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_ENGINE+ ttsEngine);
                fsClient.sendArgs(flowData.getAddress(), flowData.getUniqueId(), EslConstant.SET, EslConstant.TTS_VOICE+ ttsVoice);
            }
            errorFileName = "say:" + flowMenuNodeProperties.getErrorContent() + "'";
        }
        if(StringUtils.isBlank(errorFileName)){
            errorFileName = "silence_stream://250";
        }

        if(StringUtils.isEmpty(event)){
            fsClient.playAndGetDigits(flowData.getAddress(), flowData.getUniqueId(), 1, 1, flowMenuNodeProperties.getMaxRetries(),
                    flowMenuNodeProperties.getTimeout(), "#",
                    notFileName, errorFileName, "MENU_DTMF_RETURN",
                    "[*0-9#]+", flowMenuNodeProperties.getTimeout(), null);
        }else {
            boolean isMatch =flowMenuNodeProperties.getMenuButtons().stream().anyMatch(menuButton -> Objects.equals(menuButton.getButtonValue(),event));
            if(isMatch){
                iFlowNoticeService.notice(2, "next_"+ event, flowData);
            }else {
                fsClient.playAndGetDigits(flowData.getAddress(), flowData.getUniqueId(), 1, 1, flowMenuNodeProperties.getMaxRetries(),
                        flowMenuNodeProperties.getTimeout(), "#",
                        errorFileName, errorFileName, "MENU_DTMF_RETURN",
                        "[*0-9#]+", flowMenuNodeProperties.getTimeout(), null);
            }
        }
    }
}
