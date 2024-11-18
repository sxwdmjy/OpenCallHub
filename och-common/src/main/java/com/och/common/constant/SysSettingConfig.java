package com.och.common.constant;

import com.och.common.config.oss.AliCosConfig;
import com.och.common.config.oss.TxCosConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author danmo
 * @date 2023-10-25 10:05
 **/
@Component
@Data
@ConfigurationProperties(prefix = "system.setting")
public class SysSettingConfig {


    /**
     * freeswitch文件地址
     */
    private String fsProfile;
    /**
     * freeswitch录音文件后缀
     */
    private String fsFileSuffix;

    /**
     * 文件上传地址
     */
    private String baseProfile;

    /**
     * 文件上传域名
     */
    private String localHost;

    /**
     * 文件上传方式
     */
    private String uploadType;

    /**
     * 腾讯云cos配置
     */
    private TxCosConfig txCosConfig;

    /**
     * 阿里云云cos配置
     */
    private AliCosConfig aliCosConfig;

}
