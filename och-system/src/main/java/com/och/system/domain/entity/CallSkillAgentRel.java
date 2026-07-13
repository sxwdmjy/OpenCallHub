package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 技能坐席关联实体
 * <p>映射 call_skill_agent_rel 表，存储技能与坐席的关联关系。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_skill_agent_rel")
public class CallSkillAgentRel {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 技能ID */
    @TableField("skill_id")
    private Long skillId;

    /** 坐席ID */
    @TableField("agent_id")
    private Long agentId;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /** 逻辑删除：0-正常 1-已删除 */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

}

