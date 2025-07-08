package com.och.calltask.domain;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 客户群事件
 *
 * @author danmo
 * @date 2025/7/2 22:12
 */
@Getter
public class CustomerCrowdEvent extends ApplicationEvent {

    private final CustomerCrowdEventParam customerCrowd;

    public CustomerCrowdEvent(CustomerCrowdEventParam customerCrowd) {
        super(customerCrowd);
        this.customerCrowd = customerCrowd;
    }

}
