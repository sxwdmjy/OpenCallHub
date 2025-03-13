package com.och.file.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.och.common.annotation.FileTtsType;
import com.och.common.config.oss.AliCloudConfig;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.constant.SysSettingConfig;
import com.och.common.utils.StringUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 阿里云语音合成
 *
 * @author danmo
 * @date 2023-11-01 15:16
 **/
@FileTtsType(value = "ali")
@Slf4j
@Service
public class AlFileTtsHandler extends AbstractFileTtsHandler {
    private NlsClient client;

    private final String AliEngineTokenKey = "";

    public AlFileTtsHandler(SysSettingConfig lfsSettingConfig, RedisService redisService) {
        super(lfsSettingConfig,redisService);
    }

    @Override
    public void tts(String text, Consumer<File> fileConsumer) {
        AliCloudConfig.AliTtsConfig ttsConfig = lfsSettingConfig.getAliConfig().getTts();
        //获取token，使用时注意在accessToken.getExpireTime()过期前再次获取。
        String appToken = getAppToken(ttsConfig.getAccessKeyId(), ttsConfig.getAccessKeySecret());

        if(Objects.isNull(client)){
            if(StringUtils.isBlank(ttsConfig.getUrl())){
                client = new NlsClient(appToken);
            }else {
                client = new NlsClient(ttsConfig.getUrl(), appToken);
            }
        }else {
            client.setToken(appToken);
        }
        String fileName = DateUtil.format(DateUtil.date(), "yyyyMMddHHmmssSSS") + ".wav";
        File file = FileUtil.touch(fileName);
        process(ttsConfig.getAppKey(), text, getSynthesizerListener(file,fileConsumer));
    }

    private static SpeechSynthesizerListener getSynthesizerListener(File file,Consumer<File> fileConsumer) {
        SpeechSynthesizerListener listener = null;
        try {
            listener = new SpeechSynthesizerListener() {
                FileOutputStream fout = new FileOutputStream(file);
                private boolean firstRecvBinary = true;
                //语音合成结束
                @Override
                public void onComplete(SpeechSynthesizerResponse response) {
                    //调用onComplete时表示所有TTS数据已接收完成，因此为整个合成数据的延迟。该延迟可能较大，不一定满足实时场景。
                    System.out.println("name: " + response.getName() +
                            ", status: " + response.getStatus()+
                            ", output file :"+file.getAbsolutePath()
                    );
                    fileConsumer.accept(file);
                    try {
                        fout.close();
                    } catch (IOException e) {
                        log.error("Failed to close file output stream: {}", e.getMessage(), e);
                    }
                }
                //语音合成的语音二进制数据
                @Override
                public void onMessage(ByteBuffer message) {
                    try {
                        if(firstRecvBinary) {
                            //计算首包语音流的延迟，收到第一包语音流时，即可以进行语音播放，以提升响应速度（特别是实时交互场景下）。
                            firstRecvBinary = false;
                        }
                        byte[] bytesArray = new byte[message.remaining()];
                        message.get(bytesArray, 0, bytesArray.length);
                        fout.write(bytesArray);
                    } catch (IOException e) {
                        log.error("Failed to write to file output stream: {}", e.getMessage(), e);
                    }
                }
                @Override
                public void onFail(SpeechSynthesizerResponse response){
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
        }
        return listener;
    }

    public void process(String appKey, String text, SpeechSynthesizerListener listener) {
        SpeechSynthesizer synthesizer = null;
        try {
            //创建实例，建立连接。
            synthesizer = new SpeechSynthesizer(client, listener);
            synthesizer.setAppKey(lfsSettingConfig.getAliConfig().getTts().getAppKey());
            //设置返回音频的编码格式
            synthesizer.setFormat(OutputFormatEnum.WAV);
            //设置返回音频的采样率
            synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_8K);
            //发音人
            synthesizer.setVoice(lfsSettingConfig.getAliConfig().getTts().getVoice());
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
            e.printStackTrace();
        } finally {
            //关闭连接
            if (null != synthesizer) {
                synthesizer.close();
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        client.shutdown();
    }

    private String getAppToken(String accessKeyId, String accessKeySecret){
        boolean exists = redisService.keyIsExists(CacheConstants.ALI_TTS_TOKEN_KEY);
        if(exists){
            return redisService.getCacheObject(CacheConstants.ALI_TTS_TOKEN_KEY);
        }
        AccessToken accessToken = new AccessToken(accessKeyId, accessKeySecret);
        try {
            accessToken.apply();
            System.out.println("get token: " + accessToken.getToken() + ", expire time: " + accessToken.getExpireTime());
            long expireTime = accessToken.getExpireTime() -  DateUtil.currentSeconds();
            redisService.setCacheObject(CacheConstants.ALI_TTS_TOKEN_KEY,accessToken.getToken(), expireTime, TimeUnit.SECONDS);
            return accessToken.getToken();
        } catch (IOException e) {
           log.error("get token exception: " + e.getMessage(), e);
            return null;
        }
    }
}
