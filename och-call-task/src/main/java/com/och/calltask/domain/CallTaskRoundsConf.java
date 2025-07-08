package com.och.calltask.domain;

import lombok.Data;


/**
 * 任务轮次配置
 * @author danmo
 * @date 2025/06/25
 */
@Data
public class CallTaskRoundsConf {

    /**
     * 任务轮次
     */
    private Integer rounds;

    /**
     * 呼叫字段
     */
    private String callField;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式 0-升序 1-降序
     */
    private Integer sortType;

}
