package com.och.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class MrcpConfig {

    private static final Config config = ConfigFactory.load("mrcp.conf");

    // 获取 MRCP 服务器 IP
    public static String getServerIp() {
        return config.getString("mrcp.server.ip");
    }

    // 获取 MRCP 服务器端口
    public static int getServerPort() {
        return config.getInt("mrcp.server.port");
    }

    // 获取会话超时时间
    public static Duration getSessionTimeout() {
        return config.getDuration("mrcp.server.session.timeout");
    }

    // 获取最大消息大小（字节）
    public static Long getMaxMessageSize() {
        return config.getBytes("mrcp.server.session.max-message-size");
    }

    public static String getPlatformName() {
        return config.getString("mrcp.platform.name");
    }

    // 获取平台认证密钥
    public static String getAuthKey() {
        return config.getString("mrcp.platform.auth-key");
    }

    // 获取音频RTP端口
    public static int getAudioRtpPort() {
        return config.getInt("audio.rtp.port");
    }

    // 获取RTP端口范围最小值
    public static int getRtpMinPort() {
        return config.getInt("mrcp.rtp.port-range.min");
    }

    // 获取RTP端口范围最大值
    public static int getRtpMaxPort() {
        return config.getInt("mrcp.rtp.port-range.max");
    }

    // 编解码器信息封装类
    public static class CodecInfo {
        private final String name;
        private final int rate;
        private final int payloadType;

        public CodecInfo(String name, int rate, int payloadType) {
            this.name = name;
            this.rate = rate;
            this.payloadType = payloadType;
        }

        // Getter方法
        public String getName() {
            return name;
        }

        public int getRate() {
            return rate;
        }

        public int getPayloadType() {
            return payloadType;
        }
    }

    public static List<CodecInfo> getCodecs() {
        List<? extends Config> codecConfigs = config.getConfigList("mrcp.codecs");
        return codecConfigs.stream()
                .map(c -> new CodecInfo(
                        c.getString("name"),
                        c.getInt("rate"),
                        c.getInt("payload-type") // 读取payload-type字段
                )).collect(Collectors.toList());
    }

}
