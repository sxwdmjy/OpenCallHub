package com.och.calltask.domain;


import lombok.Data;

/**
 * @author danmo
 * @date 2025/7/4 17:21
 */
@Data
public class CustomerCrowdEventParam {

    /**
     * 人群信息
     */
    private Long crowdId;
    /**
     * 客户ID
     */
    private Long customerId;
    /**
     * 1-客户添加到群中 2-客户从群中移除 3-客户人群计算
     */
    private Integer eventType;
}
