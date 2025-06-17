
package com.och.esl.handler.esl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSONObject;
import com.och.common.annotation.EslEventName;
import com.och.common.constant.EslEventNames;
import com.och.common.domain.CallInfo;
import com.och.common.domain.DetectedSpeech;
import com.och.esl.factory.AbstractFsEslEventHandler;
import com.och.esl.utils.EslEventUtil;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * asr识别
 *
 * @author danmo
 * @date 2023年09月18日 19:03
 */
@Slf4j
@EslEventName(EslEventNames.DETECTED_SPEECH)
@Component
public class FsDetectedSpeechEslEventHandler extends AbstractFsEslEventHandler {

    @Override
    public void handleEslEvent(String address, EslEvent event) {
        log.info("DetectedSpeechEslEventHandler EslEvent:{}.", JSONObject.toJSONString(event));
        List<String> eventBodyLines = event.getEventBodyLines();
        String uniqueId = EslEventUtil.getUniqueId(event);
        CallInfo callInfo = ifsCallCacheService.getCallInfoByUniqueId(uniqueId);
        if (callInfo == null) {
            return;
        }
        if (CollectionUtil.isEmpty(eventBodyLines)) {
            return;
        }
        StringBuilder resultStr = new StringBuilder();
        for (String asrText : eventBodyLines) {
            byte[] array = new byte[asrText.toCharArray().length];
            for (int i = 0; i < asrText.toCharArray().length; i++) {
                array[i] =(byte) asrText.toCharArray()[i];
            }
            resultStr.append(new String(array, StandardCharsets.UTF_8));
        }
        DetectedSpeech speech = XmlUtil.xmlToBean(XmlUtil.parseXml(resultStr.toString()).getFirstChild(), DetectedSpeech.class);
        log.info("----------speech:{}",JSONObject.toJSONString(speech));
        fsClient.detectSpeechResume(address,callInfo.getCallId(),uniqueId);

        if(Objects.nonNull(speech.getInterpretation().getInstance())){
            DetectedSpeech.Instance instance = speech.getInterpretation().getInstance();
            String content = instance.getResult();
            log.info("======================:{}", content);
        }
    }
}
