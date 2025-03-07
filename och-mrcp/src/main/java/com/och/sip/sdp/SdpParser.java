package com.och.sip.sdp;

import java.util.ArrayList;
import java.util.List;

public class SdpParser {

    /**
     * 通用解析方法，填充 SdpMessage 子类
     *
     * @param sdpContent SDP 字符串
     * @param message    目标对象（SdpOffer 或 SdpAnswer）
     */
    public static void parse(String sdpContent, SdpMessage message) {
        String[] lines = sdpContent.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("v=")) {
                message.setVersion(line.substring(2));
            } else if (line.startsWith("o=")) {
                message.setOrigin(line.substring(2));
            } else if (line.startsWith("s=")) {
                message.setSessionName(line.substring(2));
            } else if (line.startsWith("c=")) {
                message.setConnectionInfo(line.substring(2));
            } else if (line.startsWith("t=") && message instanceof SdpAnswer) {
                ((SdpAnswer) message).setTimingInfo(line.substring(2));
            } else if (line.startsWith("m=")) {
                parseMediaDescription(line, message);
            } else if (line.startsWith("a=")) {
                parseAttribute(line, message);
            }
        }
    }

    private static void parseMediaDescription(String line, SdpMessage message) {
        String[] parts = line.substring(2).split(" ");
        if (parts.length < 4) return;

        SdpMessage.MediaDescription md = new SdpMessage.MediaDescription();
        md.setMediaType(parts[0]);
        md.setPort(Integer.parseInt(parts[1]));
        md.setProtocol(parts[2]);

        // 解析格式参数（负载类型列表）
        List<String> formats = new ArrayList<>();
        for (int i = 3; i < parts.length; i++) {
            formats.add(parts[i]);
        }
        md.setFormats(formats);
        message.addMediaDescription(md);
    }

    private static void parseAttribute(String line, SdpMessage message) {
        String[] keyValue = line.substring(2).split(":", 2);
        if (keyValue.length == 2 && !message.getMediaDescriptions().isEmpty()) {
            SdpMessage.MediaDescription lastMd = message.getMediaDescriptions().get(
                    message.getMediaDescriptions().size() - 1
            );
            lastMd.addAttribute(keyValue[0].trim(), keyValue[1].trim());
        }else if (keyValue.length == 1 && !message.getMediaDescriptions().isEmpty()) {
            SdpMessage.MediaDescription lastMd = message.getMediaDescriptions().get(
                    message.getMediaDescriptions().size() - 1
            );
            lastMd.addAttribute(keyValue[0].trim(), "");
        }
    }
}