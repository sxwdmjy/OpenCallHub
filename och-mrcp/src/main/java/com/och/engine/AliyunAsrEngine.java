package com.och.engine;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.och.mrcp.MrcpRequest;
import com.och.mrcp.MrcpResponse;
import com.och.mrcp.MrcpSession;
import com.och.redis.RedissonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AliyunAsrEngine implements AsrEngine {

    private volatile NlsClient client;

    private final String AliEngineTokenKey = "ali_engine_token";

    private final CloudConfig config;

    private SpeechTranscriber transcriber;

    public AliyunAsrEngine() {
        this.config = CloudConfigManager.getConfig("aliyun");
        init();
    }

    //单例获取NlsClient
    private void init() {
        String rtpEngineAddress = config.getEndpoint();
        String accessToken = getAccessToken(config);
        if (rtpEngineAddress.isEmpty()) {
            client = new NlsClient(accessToken);
        } else {
            client = new NlsClient(rtpEngineAddress, accessToken);
        }

    }


    @Override
    public void recognize(byte[] audioData) {
            if (transcriber != null){
                transcriber.send(audioData);
            }
    }


    @Override
    public void start(MrcpRequest req, MrcpSession mrcpSession) {
        try {
            if (!RedissonUtil.isExists(AliEngineTokenKey)) {
                client.setToken(getAccessToken(config));
            }
            //创建实例、建立连接。
            SpeechTranscriber speechTranscriber = new SpeechTranscriber(client, getTranscriberListener(mrcpSession, req));
            speechTranscriber.setAppKey(config.getAppKey());
            //输入音频编码方式。
            speechTranscriber.setFormat(InputFormatEnum.PCM);
            //输入音频采样率。
            speechTranscriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_8K);
            //是否返回中间识别结果。
            speechTranscriber.setEnableIntermediateResult(false);
            //是否生成并返回标点符号。
            speechTranscriber.setEnablePunctuation(true);
            //是否将返回结果规整化，比如将一百返回为100。
            speechTranscriber.setEnableITN(false);
            speechTranscriber.start();
            setTranscriber(speechTranscriber);
        } catch (Exception e) {
            log.error("ASR exception: " + e.getMessage(), e);
        }
    }

    @Override
    public void end() {
        transcriber.close();
        transcriber = null;
    }



    private String getAccessToken(CloudConfig config) {
        AccessToken accessToken = new AccessToken(config.getApiKey(), config.getApiSecret());
        try {
            accessToken.apply();
            log.info("get token: " + ", expire time: " + accessToken.getExpireTime());
            long expireTime = accessToken.getExpireTime() - System.currentTimeMillis() / 1000;
            RedissonUtil.setValue(AliEngineTokenKey, accessToken.getToken(), expireTime, TimeUnit.SECONDS);
        } catch (IOException e) {
            log.error("get token exception: " + e.getMessage(), e);
            return null;
        }
        return accessToken.getToken();
    }

    public SpeechTranscriberListener getTranscriberListener(MrcpSession mrcpSession, MrcpRequest req) {

        return new SpeechTranscriberListener() {
            //识别出中间结果。仅当setEnableIntermediateResult为true时，才会返回该消息。
            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                log.info("onTranscriptionResultChange task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        //状态码“20000000”表示正常识别。
                        ", status: " + response.getStatus() +
                        //句子编号，从1开始递增。
                        ", index: " + response.getTransSentenceIndex() +
                        //当前的识别结果。
                        ", result: " + response.getTransSentenceText() +
                        //当前已处理的音频时长，单位为毫秒。
                        ", time: " + response.getTransSentenceTime());
            }

            @Override
            public void onTranscriberStart(SpeechTranscriberResponse response) {
                //task_id是调用方和服务端通信的唯一标识，遇到问题时，需要提供此task_id。
                log.info("onTranscriberStart task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());
            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {
                log.info("onSentenceBegin task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());

            }

            //识别出一句话。服务端会智能断句，当识别到一句话结束时会返回此消息。
            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                log.info("onSentenceEnd task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus() + ",result：" + response.getTransSentenceText());
                MrcpResponse res = new MrcpResponse();
                res.setVersion(req.getVersion());
                res.setStatusCode(393);
                res.setMethod("RECOGNITION-COMPLETE");
                res.setRequestId(req.getRequestId());
                res.setStatusText("COMPLETE");
                res.addHeader("Channel-Identifier", mrcpSession.getSessionId());
                res.addHeader("Completion-Cause", "000 success");
                res.addHeader("Content-Type", "application/json");
                JSONObject interpretation = new JSONObject();
                interpretation.put("grammar", req.getBody());
                interpretation.put("confidence", response.getConfidence());
                interpretation.put("instance", response.getTransSentenceText());
                JSONObject input = new JSONObject();
                input.put("mode", "speech");
                input.put("value", response.getTransSentenceText());
                interpretation.put("input", input);

                JSONObject result = new JSONObject();
                result.put("interpretation", interpretation);

                JSONObject gresponse = new JSONObject();
                gresponse.put("result", result);

                res.setBody(gresponse.toJSONString());
                res.addHeader("Content-Length", String.valueOf(res.getBody().length()));
                mrcpSession.process(res);
            }

            //识别完毕
            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                log.info("onTranscriptionComplete task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus() + ",result：" + response.getTransSentenceText());
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
                //task_id是调用方和服务端通信的唯一标识，遇到问题时，需要提供此task_id。
                log.info("onFail task_id: " + response.getTaskId() + ", status: " + response.getStatus() + ", status_text: " + response.getStatusText());
            }
        };
    }


    public void setTranscriber(SpeechTranscriber transcriber) {
        this.transcriber = transcriber;
    }
}
