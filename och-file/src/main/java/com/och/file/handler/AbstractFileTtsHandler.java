package com.och.file.handler;

import cn.hutool.core.date.DateUtil;
import com.och.common.annotation.FileTtsType;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.SysSettingConfig;
import com.och.common.utils.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.function.Consumer;

/**
 * @author danmo
 * @date 2023-11-01 15:26
 **/
@FileTtsType(value = "ali")
public abstract class AbstractFileTtsHandler {

    protected SysSettingConfig lfsSettingConfig;

    protected RedisService redisService;

    public AbstractFileTtsHandler(SysSettingConfig lfsSettingConfig, RedisService redisService) {
        this.lfsSettingConfig = lfsSettingConfig;
        this.redisService = redisService;
    }

    /**
     * 文本转语音
     * @param text 文本
     * @param consumer 文件回调
     */
    public abstract void tts(String text, Consumer<File> consumer);


    /**
     * 获取上传文件地址
     * @return
     */
    public String getFileTempPath(){
        Date date = new Date();
        return lfsSettingConfig.getBaseProfile() + "/" + "voice" + "/" + DateUtil.year(date) + "/" + DateUtil.month(date) + "/" + DateUtil.dayOfMonth(date) + "/";
    }
}
