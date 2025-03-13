package com.och.ivr.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowPlaybackNodeProperties extends FlowNodeProperties{

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
     * 播放内容
     */
    private String content;

    /**
     * 播放次数
     */
    private Integer num;
}
