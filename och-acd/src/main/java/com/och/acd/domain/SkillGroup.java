package com.och.acd.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 技能组实体
 * <p>
 * 呼叫中心的技能组配置，定义队列容量、最大等待时间、分配策略、
 * 溢出目标、服务时间等。座席按技能组分组管理和分配呼叫。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("acd_skill_group")
public class SkillGroup {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupName;
    private String groupCode;
    private Integer maxQueueSize;
    private Integer maxWaitSeconds;
    private String dispatchStrategy;
    private String overflowGroupId;
    private String serviceHoursJson;
    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    private String remark;
}
