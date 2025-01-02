package com.och.ivr.properties;

import lombok.Data;

/**
 * 开始节点属性参数
 * @author: danmo
 * @date 2024/12/29 19:05
 */
@Data
public class FlowStartNodeProperties implements FlowNodeProperties{

    private Long asrEngine;

    private Long ttsEngine;
}
