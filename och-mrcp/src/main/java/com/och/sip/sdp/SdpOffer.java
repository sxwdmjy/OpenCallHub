package com.och.sip.sdp;

import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SDP Offer消息 (RFC 3264 §4)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpOffer extends SdpMessage {
    private String sessionInfo;    // i=字段（可选）
    private String bandwidthInfo;  // b=字段（可选）

    @Override
    public void parse(String sdp) {
        SdpParser.parse(sdp, this); // 调用通用解析方法
    }

    @Override
    public String toString() {
        SdpBuilder builder = new SdpBuilder()
                .append("v=").append(version).newLine()
                .append("o=").append(origin).newLine()
                .append("s=").append(sessionName).newLine()
                .appendOptional("i=", sessionInfo)
                .append("c=").append(connectionInfo).newLine()
                .appendOptional("b=", bandwidthInfo);

        for (MediaDescription md : mediaDescriptions) {
            builder.append("m=")
                    .append(md.getMediaType()).space()
                    .append(md.getPort()).space()
                    .append(md.getProtocol()).space()
                    .append(String.join(" ", md.getFormats())).newLine();

            for (String key : md.getAttributes().keySet()) {
                for (String value : md.getAttributes().get(key)) {
                    {
                        if (StringUtil.isNullOrEmpty(value)) {
                            builder.append("a=").append(key).newLine();
                        } else {
                            builder.append("a=").append(key).append(":").append(value).newLine();
                        }
                    }
                }
            }

        }

        return builder.build();
    }

    public void setSessionInfo(String sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public String getSessionInfo() {
        return sessionInfo;
    }

    public void setBandwidthInfo(String bandwidthInfo) {
        this.bandwidthInfo = bandwidthInfo;
    }

    public String getBandwidthInfo() {
        return bandwidthInfo;
    }
}
