package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 技能坐席关联表(CallSkillAgentRel)表实体类
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_skill_agent_rel")
public class CallSkillAgentRel extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -10902663282669484L;

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 技能ID
     */
    @Schema(description = "技能ID")
    @TableField("skill_id")
    private Long skillId;


    /**
     * 坐席ID
     */
    @Schema(description = "坐席ID")
    @TableField("agent_id")
    private String agentId;


    /**
     * 级别
     */
    @Schema(description = "级别")
    @TableField("level")
    private Integer level;


}

