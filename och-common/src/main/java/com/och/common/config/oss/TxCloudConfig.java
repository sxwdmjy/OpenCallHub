package com.och.common.config.oss;

import lombok.Data;

/**
 * 腾讯云存储配置
 */
@Data
public class TxCloudConfig {


    /**
     * 腾讯云存储
     */
    private TxCosConfig cos;

    private TxTtsConfig tts;


    @Data
    public static class TxCosConfig {


        /**
         * 腾讯云域名
         */
        private String host;
        /**
         * 桶名称
         */
        private String bucketName;

        /**
         * 地区
         */
        private String regionName;

        /**
         * 密钥key
         */
        private String accessKey;

        /**
         * 密钥
         */
        private String secretKey;
    }

    @Data
    public static class TxTtsConfig {

    }
}
