package com.och.sip.sdp;

import com.och.config.MrcpConfig;
import com.och.rtp.PortPoolManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AudioVideoStrategy implements SdpStrategy {

    private static final String MRCP_FORMAT = "1"; // MRCPv2 使用格式参数1

    @Override
    public SdpAnswer negotiate(SdpOffer offer) {
        SdpAnswer answer = new SdpAnswer();
        copyCommonFields(offer, answer);

        for (SdpMessage.MediaDescription md : offer.getMediaDescriptions()) {
            if ("application".equals(md.getMediaType()) && "TCP/MRCPv2".equals(md.getProtocol())) {
                // 处理MRCPv2
                SdpMessage.MediaDescription mrcpMd = processMrcp(md);
                answer.addMediaDescription(mrcpMd);
            } else if ("audio".equals(md.getMediaType())) {
                // 处理音频编解码器
                SdpMessage.MediaDescription audioMd = processAudio(md);
                answer.addMediaDescription(audioMd);
            }
        }
        return answer;
    }

    /**
     * 复制 SDP Offer 的通用字段到 Answer
     */
    private void copyCommonFields(SdpOffer offer, SdpAnswer answer) {
        // 复制基类 SdpMessage 的公共字段
        answer.setVersion(offer.getVersion());
        answer.setOrigin(offer.getOrigin());
        answer.setSessionName(offer.getSessionName());
        answer.setConnectionInfo(offer.getConnectionInfo());

        // 设置 Answer 特有的字段
        answer.setTimingInfo("0 0"); // 默认时间范围（t=0 0）

    }

    /**
     * 处理 MRCP 资源描述
     */
    private SdpMessage.MediaDescription processMrcp(SdpMessage.MediaDescription offerMd) {
        // 检查Offer是否支持MRCPv2
        List<String> offerFormats = offerMd.getFormats();
        if (!offerFormats.contains(MRCP_FORMAT)) {
            return null; // 不支持MRCPv2，拒绝该媒体流
        }

        // 生成Answer媒体描述
        SdpMessage.MediaDescription answerMd = new SdpMessage.MediaDescription();
        answerMd.setMediaType("application");
        answerMd.setPort(MrcpConfig.getServerPort());
        answerMd.setProtocol("TCP/MRCPv2");
        answerMd.setFormats(Collections.singletonList(MRCP_FORMAT));
        answerMd.addAttribute("setup", "passive");
        answerMd.addAttribute("connection", "new");
        answerMd.addAttribute("channel", System.currentTimeMillis()+"@speechrecog");
        answerMd.addAttribute("cmid", "1");
        return answerMd;
    }

    private SdpMessage.MediaDescription processAudio(SdpMessage.MediaDescription offerMd) {
        SdpMessage.MediaDescription answerMd = new SdpMessage.MediaDescription();
        answerMd.setMediaType("audio");

        // 从端口池动态分配唯一端口
        int selectedPort = PortPoolManager.getInstance().allocatePort();
        answerMd.setPort(selectedPort);

        answerMd.setProtocol(offerMd.getProtocol());

        // 获取支持的编解码器负载类型
        List<String> supportedPayloads = MrcpConfig.getCodecs().stream()
                .map(codec -> String.valueOf(codec.getPayloadType()))
                .toList();

        // 取交集（客户端Offer与服务器支持的编解码器）
        List<String> acceptedPayloads = offerMd.getFormats().stream()
                .filter(supportedPayloads::contains)
                .collect(Collectors.toList());

        // 添加telephone-event支持
        if (!acceptedPayloads.contains("101")) {
            acceptedPayloads.add("101");
        }

        answerMd.setFormats(acceptedPayloads);

        // 添加rtpmap属性
        MrcpConfig.getCodecs().forEach(codec -> {
            if (acceptedPayloads.contains(String.valueOf(codec.getPayloadType()))) {
                answerMd.addAttribute("rtpmap",
                        codec.getPayloadType() + " " + codec.getName() + "/" + codec.getRate()
                );
            }
        });
        answerMd.addAttribute("rtpmap", "101 telephone-event/8000");
        answerMd.addAttribute("fmtp", "101 0-15");
        answerMd.addAttribute("recvonly", "");
        answerMd.addAttribute("ptime", "20");
        answerMd.addAttribute("mid", "1");

        return answerMd;
    }

}
