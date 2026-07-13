package com.och.ivr.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * IVR流程实体
 * <p>
 * 定义IVR流程的元数据，包括流程名称、编码、当前版本、已发布版本等。
 * 流程的详细节点和边通过版本（versionId）关联。
 * </p>
 */
@Data
@TableName("ivr_flow")
public class IvrFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String flowName;
    private String flowCode;
    private Integer currentVersion;
    private Long publishedVersionId;
    private Boolean enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    private String remark;
}
