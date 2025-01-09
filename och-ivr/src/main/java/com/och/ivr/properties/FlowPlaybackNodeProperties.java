package com.och.ivr.properties;

import lombok.Data;

@Data
public class FlowPlaybackNodeProperties implements FlowNodeProperties{

    /**
     * 打断
     */
    private Boolean interrupt;

    /**
     * 播放类型 0-文件 1-内容
     */
    private Integer playbackType;

    /**
     * 播放文件
     */
    private Long fileId;

    /**
     * 播放文件
     */
    private String file;

    /**
     * 播放内容
     */
    private String content;
}
