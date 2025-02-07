package com.och.ivr.properties;

import lombok.Data;

/**
 * 开始节点属性参数
 * @author: danmo
 * @date 2024/12/29 19:05
 */
@Data
public class FlowStartNodeProperties implements FlowNodeProperties{

    // 是否录音
    private Boolean recording;

    // 是否开启asr
    private Long asrEngine;

    // 是否开启tts
    private Long ttsEngine;
}
