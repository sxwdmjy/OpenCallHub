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
 * 意图信息(IntentInfo)表实体类
 *
 * @author danmo
 * @since 2025-10-17 14:54:11
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("intent_info")
public class IntentInfo extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -51728701653422504L;

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 分组ID
     */
    @Schema(description = "分组ID")
    @TableField("group_id")
    private Long groupId;


    /**
     * 意图名称
     */
    @Schema(description = "意图名称")
    @TableField("name")
    private String name;


    /**
     * 意图编码
     */
    @Schema(description = "意图编码")
    @TableField("code")
    private String code;


    /**
     * 识别方式 1-规则 2-向量 3-混合
     */
    @Schema(description = "识别方式 1-规则 2-向量 3-混合")
    @TableField("type")
    private Integer type;


    /**
     * 匹配置信度阈值
     */
    @Schema(description = "匹配置信度阈值")
    @TableField("confidence_threshold")
    private Float confidenceThreshold;


}

