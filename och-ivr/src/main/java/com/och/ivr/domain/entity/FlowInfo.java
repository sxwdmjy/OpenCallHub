package com.och.ivr.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * ivr流程信息(FlowInfo)表实体类
 *
 * @author danmo
 * @since 2024-12-23 15:08:24
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("flow_info")
public class FlowInfo extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 676718916120461321L;

    /**
     * 流程实例唯一标识符
     */

    @Schema(description = "流程实例唯一标识符")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 分组ID
     */
    @Schema(description = "分组ID")
    @TableField("group_id")
    private Long groupId;


    /**
     * ivr名称
     */
    @Schema(description = "ivr名称")
    @TableField("name")
    private String name;


    /**
     * 流程描述
     */
    @Schema(description = "流程描述")
    @TableField("desc")
    private String desc;


    /**
     * 流程状态 0-草稿 1-待发布 2-已发布
     */
    @Schema(description = "流程状态 0-草稿 1-待发布 2-已发布 3-已下线")
    @TableField("status")
    private Integer status;


}

