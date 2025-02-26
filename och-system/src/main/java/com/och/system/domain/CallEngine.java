package com.och.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * AI引擎表(CallEngine)表实体类
 *
 * @author danmo
 * @since 2025-02-24 14:49:07
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_engine")
public class CallEngine extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 493932373193095699L;

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 名称
     */
    @Schema(description = "名称")
    @TableField("name")
    private String name;


    /**
     * 引擎模块名称
     */
    @Schema(description = "引擎模块名称")
    @TableField("profile")
    private String profile;


    /**
     * 引擎类型 1-asr 2-tts
     */
    @Schema(description = "引擎类型 1-asr 2-tts")
    @TableField("type")
    private Integer type;


    /**
     * 服务商 1-阿里 2-腾讯 3-讯飞
     */
    @Schema(description = "服务商 1-阿里 2-腾讯 3-讯飞")
    @TableField("providers")
    private Integer providers;


    /**
     * 发声音色
     */
    @Schema(description = "发声音色")
    @TableField("timbre")
    private String timbre;


    /**
     * asr语法
     */
    @Schema(description = "asr语法")
    @TableField("grammar")
    private String grammar;


    /**
     * 参数
     */
    @Schema(description = "参数")
    @TableField("param")
    private String param;


}

