package com.och.resource.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 电话号码实体，代表一个外线电话号码。
 * 号码可归属于号码池，用于呼入呼出时的主叫或被叫显示。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("res_phone_number")
public class PhoneNumber {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String phoneNumber;
    private Long numberPoolId;
    private String provider;
    private String lineType;
    private String areaCode;
    private Boolean enabled;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private Long updatedBy;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    private String status;
    private String remark;
}
