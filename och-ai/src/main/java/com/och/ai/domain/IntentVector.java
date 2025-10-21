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
 * 意图向量表(IntentVector)表实体类
 *
 * @author danmo
 * @since 2025-10-17 14:54:11
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("intent_vector")
public class IntentVector extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 379841907415972051L;

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
     * 示例语句
     */
    @Schema(description = "示例语句")
    @TableField("text")
    private String text;


    /**
     * 向量ID
     */
    @Schema(description = "向量ID")
    @TableField("vector_id")
    private String vectorId;


    /**
     * 是否启用 0-关闭 1-启用
     */
    @Schema(description = "是否启用 0-关闭 1-启用")
    @TableField("status")
    private Integer status;


}

