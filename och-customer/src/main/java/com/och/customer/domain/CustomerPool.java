package com.och.customer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 客户池 */
@Data @TableName("customer_pool")
public class CustomerPool {
    @TableId(type = IdType.AUTO) private Long id;
    private String poolName;
    private String poolCode;
    private String recycleRuleJson;    // 回收规则JSON
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableLogic private Integer deleted;
    private String remark;
}
