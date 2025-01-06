package com.och.esl.handler.route;

import cn.hutool.core.date.DateUtil;
import com.och.common.annotation.EslRouteName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
import com.och.common.enums.RouteTypeEnum;
import com.och.system.domain.vo.file.VoiceFileVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author danmo
 * @date 2023-11-10 17:20
 **/
@EslRouteName(RouteTypeEnum.VOICE)
@Component
@Slf4j
public class FsVoiceRouteHandler extends FsAbstractRouteHandler {


    @Override
    public void handler(String address, CallInfo callInfo, String uniqueId, String voiceId) {
        log.info("转放音 callId:{} transfer to {}", callInfo.getCallId(), voiceId);
        VoiceFileVo voiceFileVo = iVoiceFileService.getDetail(Long.valueOf(voiceId));
        if(Objects.isNull(voiceFileVo)){
            log.info("转放音获取文件失败 callId:{} transfer to {}", callInfo.getCallId(), voiceId);
            fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
        }

        CallInfoDetail detail = new CallInfoDetail();
        detail.setCallId(callInfo.getCallId());
        detail.setStartTime(DateUtil.current());
        detail.setOrderNum(callInfo.getDetailList() == null ? 0 : callInfo.getDetailList().size() + 1);
        detail.setTransferType(4);

        fsClient.playFile(address, uniqueId, voiceFileVo.getFilePath());

        detail.setEndTime(DateUtil.current());
        callInfo.addDetailList(detail);
        fsCallCacheService.saveCallInfo(callInfo);
    }
}
