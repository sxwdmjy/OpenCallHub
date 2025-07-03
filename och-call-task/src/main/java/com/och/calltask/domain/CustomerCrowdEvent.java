package com.och.calltask.domain;


import com.och.calltask.domain.vo.CustomerCrowdVo;
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

    private final CustomerCrowdVo customerCrowd;

    public CustomerCrowdEvent(CustomerCrowdVo customerCrowd) {
        super(customerCrowd.getId());
        this.customerCrowd = customerCrowd;
    }
}
