package com.och.sip.sdp;

import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SDP Answer消息 (RFC 3264 §5)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpAnswer extends SdpMessage {
    private String timingInfo;     // t=字段

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
                .append("c=").append(connectionInfo).newLine()
                .append("t=").append(timingInfo).newLine();

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

    public void setTimingInfo(String timingInfo) {
        this.timingInfo = timingInfo;
    }

    public String getTimingInfo() {
        return timingInfo;
    }

}