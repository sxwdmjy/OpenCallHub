package com.och.ivr.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowMenuNodeProperties extends FlowNodeProperties{

    /**
     * 是否支持打断 true:支持 false:不支持
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
     * 未按键播放类型 0-文件 1-内容
     */
    private Integer notPlaybackType;

    /**
     * 未按键播放文件
     */
    private Long notFileId;

    /**
     * 未按键播放内容
     */
    private String notContent;

    /**
     * 错按键播放类型 0-文件 1-内容
     */
    private Integer errorPlaybackType;

    /**
     * 错按键播放文件
     */
    private Long errorFileId;

    /**
     * 错按键播放内容
     */
    private String errorContent;


    /**
     * 菜单按钮
     */
    private List<MenuButton> menuButtons;

    /**
     * 菜单超时时间
     */
    private Integer timeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    @Data
    public static class MenuButton {
        /**
         * 按键名称
         */
        private String buttonName;
        /**
         * 按键值
         */
        private String buttonValue;
    }
}
