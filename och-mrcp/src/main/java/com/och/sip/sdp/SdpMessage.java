package com.och.sip.sdp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import io.netty.util.internal.StringUtil;
import lombok.Data;

import java.util.*;

@Data
public abstract class SdpMessage {
    protected String version = "0";            // v=0
    protected String origin;                   // o=字段
    protected String sessionName;              // s=字段
    protected String connectionInfo;           // c=字段
    protected final List<MediaDescription> mediaDescriptions = new ArrayList<>();

    /**
     * SDP媒体描述类 (RFC 4566 §5)
     */
    @Data
    public static class MediaDescription {
        private String mediaType;  // m=字段类型 (audio/video)
        private int port;          // 端口号
        private String protocol;   // 传输协议 (如RTP/AVP)
        private List<String> formats = new ArrayList<>(); // 新增格式列表
        private ListMultimap<String, String> attributes = ArrayListMultimap.create();// a=属性

        public void addAttribute(String key, String value) {
            attributes.put(key, value.trim());
        }
    }

    public abstract String toString();

    public abstract void parse(String sdp);

    public void addMediaDescription(MediaDescription mediaDescription) {
        if (mediaDescription != null) {
            // Answer需要特殊处理（如端口置零表示拒绝）
            if (!isMediaSupported(mediaDescription) && !Objects.equals("application", mediaDescription.getMediaType())) {
                mediaDescription.setPort(0); // RFC 3264 §5 拒绝方式
            }
            mediaDescriptions.add(mediaDescription);
        }
    }

    private boolean isMediaSupported(MediaDescription md) {
        //仅支持音频和视频
        return md.getMediaType().matches("audio|video");
    }

    public void setOrigin(String origin) {
        if(origin.contains("UniMRCPClient")){
            origin =  origin.replace("UniMRCPClient", "UniMRCPServer");
        }
        this.origin = origin;
    }
}