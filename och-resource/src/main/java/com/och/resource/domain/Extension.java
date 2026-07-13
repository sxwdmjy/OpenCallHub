package com.och.resource.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分机实体，表示电话系统中的内部分机号。
 * 分机必须关联到一个SIP账号，用于坐席或用户的内部通话。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("res_extension")
public class Extension {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String extensionNumber;
    private Long sipAccountId;
    private Long userId;
    private String displayName;
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
