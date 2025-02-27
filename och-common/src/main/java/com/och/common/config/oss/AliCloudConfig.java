package com.och.common.config.oss;

import lombok.Data;

/**
 * 阿里云存储配置
 */
@Data
public class AliCloudConfig {
    /**
     * 阿里云存储
     */
    private AliCosConfig cos;

    private AliTtsConfig tts;


    @Data
    public static class AliCosConfig {
        /**
         * 阿里云域名
         */
        private String host;

        /**
         * 域名
         */
        private String endpoint;

        /**
         * 桶名称
         */
        private String bucketName;

        /**
         * 桶区域
         */
        private String region;

        /**
         * 应用ID
         */
        private String accessKeyId;
        /**
         * 应用密钥
         */
        private String accessKeySecret;
    }

    @Data
    public static class AliTtsConfig {
        /**
         * appKey
         */
        private String appKey;
        /**
         * 应用ID
         */
        private String accessKeyId;
        /**
         * 应用密钥
         */
        private String accessKeySecret;

        /**
         * 语音合成地址
         */
        private String url;

        /**
         * 音色
         */
        private String voice;
    }
}
