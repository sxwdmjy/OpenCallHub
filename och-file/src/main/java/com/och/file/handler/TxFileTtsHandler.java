package com.och.file.handler;

import com.och.common.annotation.FileTtsType;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.SysSettingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.function.Consumer;

/**
 * 腾讯语音合成
 *
 * @author danmo
 * @date 2023-11-01 15:16
 **/
@FileTtsType(value = "tx")
@Slf4j
@Service
public class TxFileTtsHandler extends AbstractFileTtsHandler {


    public TxFileTtsHandler(SysSettingConfig lfsSettingConfig, RedisService redisService) {
        super(lfsSettingConfig, redisService);
    }

    @Override
    public void tts(String text, Consumer<File> consumer) {

    }
}
