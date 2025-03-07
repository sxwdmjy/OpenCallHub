package com.och.engine;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.och.mrcp.MrcpResponse;
import com.och.redis.RedissonUtil;
import com.och.utils.G711Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class AliyunTtsEngine implements TtsEngine {


    private final CloudConfig config;

    private NlsClient client;

    public AliyunTtsEngine() {
        this.config = CloudConfigManager.getConfig("aliyun");
        getAppToken(config.getApiKey(), config.getApiSecret());
    }

    private static SpeechSynthesizerListener getSynthesizerListener(BiConsumer<byte[], Throwable> msgConsumer, Consumer<JSONObject> completeConsumer) {
        SpeechSynthesizerListener listener = null;
        try {
            listener = new SpeechSynthesizerListener() {
                //语音合成结束
                @Override
                public void onComplete(SpeechSynthesizerResponse response) {
                    //调用onComplete时表示所有TTS数据已接收完成，因此为整个合成数据的延迟。该延迟可能较大，不一定满足实时场景。
                    System.out.println("name: " + response.getName() + ", status: " + response.getStatus());
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("status", response.getStatus());
                    responseJson.put("taskId", response.getTaskId());
                    responseJson.put("name", response.getName());
                    responseJson.put("statusText", response.getStatusText());
                    completeConsumer.accept(responseJson);
                }

                //语音合成的语音二进制数据
                @Override
                public void onMessage(ByteBuffer message) {
                    // 获取原始音频数据
                    byte[] payload = new byte[message.remaining()];
                    message.get(payload, 0, payload.length);
                    msgConsumer.accept(payload, null);
                }

                @Override
                public void onFail(SpeechSynthesizerResponse response) {
                    //task_id是调用方和服务端通信的唯一标识，当遇到问题时需要提供task_id以便排查。
                    System.out.println(
                            "task_id: " + response.getTaskId() +
                                    //状态码 20000000 表示识别成功
                                    ", status: " + response.getStatus() +
                                    //错误信息
                                    ", status_text: " + response.getStatusText());
                }
            };
        } catch (Exception e) {
            log.error("Failed to create synthesizer listener: {}", e.getMessage(), e);
            msgConsumer.accept(null, new IOException(e));
        }
        return listener;
    }

    public void process(String text, String voice, SpeechSynthesizerListener listener) {
        SpeechSynthesizer synthesizer = null;
        try {
            String appToken = getAppToken(config.getApiKey(), config.getApiSecret());
            if (Objects.isNull(client)) {
                if (StringUtil.isNullOrEmpty(config.getEndpoint())) {
                    client = new NlsClient(appToken);
                } else {
                    client = new NlsClient(config.getEndpoint(), appToken);
                }
            } else {
                client.setToken(appToken);
            }
            //创建实例，建立连接。
            synthesizer = new SpeechSynthesizer(client, listener);
            synthesizer.setAppKey(config.getAppKey());
            //设置返回音频的编码格式
            synthesizer.setFormat(OutputFormatEnum.PCM);
            //设置返回音频的采样率
            synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_8K);
            //发音人
            synthesizer.setVoice(voice);
            //语调，范围是-500~500，可选，默认是0。
            //synthesizer.setPitchRate(100);
            //语速，范围是-500~500，默认是0。
            // synthesizer.setSpeechRate(-100);
            //设置用于语音合成的文本
            synthesizer.setText(text);
            // 是否开启字幕功能（返回相应文本的时间戳），默认不开启，需要注意并非所有发音人都支持该参数。
            synthesizer.addCustomedParam("enable_subtitle", false);
            //此方法将以上参数设置序列化为JSON格式发送给服务端，并等待服务端确认。
            long start = System.currentTimeMillis();
            synthesizer.start();
            log.info("tts start latency " + (System.currentTimeMillis() - start) + " ms");
            //等待语音合成结束
            synthesizer.waitForComplete();
            log.info("tts stop latency " + (System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            log.error("Failed to synthesize text: {}", e.getMessage(), e);
        } finally {
            //关闭连接
            if (null != synthesizer) {
                synthesizer.close();
            }
        }
    }


    @Override
    public void synthesize(String text, String voice, TtsCallback callback) {
        process(text, voice, getSynthesizerListener((bytes, error) -> {
            callback.onComplete(bytes, null);
        }, callback::onComplete));
    }

    private String getAppToken(String accessKeyId, String accessKeySecret) {
        String aliTTsEngineTokenKey = "ali_tts_engine_token";
        Boolean exists = RedissonUtil.isExists(aliTTsEngineTokenKey);
        if (exists) {
            return RedissonUtil.getValue(aliTTsEngineTokenKey);
        }
        AccessToken accessToken = new AccessToken(accessKeyId, accessKeySecret);
        try {
            accessToken.apply();
            System.out.println("get token: " + accessToken.getToken() + ", expire time: " + accessToken.getExpireTime());
            long expireTime = accessToken.getExpireTime() - DateUtil.currentSeconds();
            RedissonUtil.setValue(aliTTsEngineTokenKey, accessToken.getToken(), expireTime, TimeUnit.SECONDS);
            return accessToken.getToken();
        } catch (IOException e) {
            log.error("get token exception: " + e.getMessage(), e);
            return null;
        }
    }
}