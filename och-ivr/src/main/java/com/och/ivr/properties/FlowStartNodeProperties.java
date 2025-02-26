package com.och.ivr.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开始节点属性参数
 * @author: danmo
 * @date 2024/12/29 19:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FlowStartNodeProperties extends FlowNodeProperties{


    // 是否录音
    private Boolean recording;

    // 是否开启asr
    private Long asrEngine;

    // 是否开启tts
    private Long ttsEngine;


}
