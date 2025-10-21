package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 意图规则表(IntentPattern)表实体类
 *
 * @author danmo
 * @since 2025-10-17 14:54:11
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("intent_pattern")
public class IntentPattern extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -34591772989071881L;

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 所属意图
     */
    @Schema(description = "所属意图")
    @TableField("intent_id")
    private Long intentId;


    /**
     * 规则类型 1-关键词 2-正则
     */
    @Schema(description = "规则类型 1-关键词 2-正则")
    @TableField("match_type")
    private Integer matchType;


    /**
     * 匹配关键字或正则
     */
    @Schema(description = "匹配关键字或正则")
    @TableField("pattern")
    private String pattern;


    /**
     * 匹配权重
     */
    @Schema(description = "匹配权重")
    @TableField("weight")
    private Float weight;


    /**
     * 是否启用 0-关闭 1-启用
     */
    @Schema(description = "是否启用 0-关闭 1-启用")
    @TableField("status")
    private Integer status;


}

