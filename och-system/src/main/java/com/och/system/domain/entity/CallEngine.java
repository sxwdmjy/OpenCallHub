package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * AI引擎实体
 * <p>映射 call_engine 表，存储 AI 引擎配置信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_engine")
public class CallEngine {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称 */
    @TableField("name")
    private String name;

    /** 引擎模块名称 */
    @TableField("profile")
    private String profile;

    /** 引擎类型：1-asr 2-tts */
    @TableField("type")
    private Integer type;

    /** 服务商：1-阿里 2-腾讯 3-讯飞 */
    @TableField("providers")
    private Integer providers;

    /** 发声音色 */
    @TableField("timbre")
    private String timbre;

    /** ASR语法 */
    @TableField("grammar")
    private String grammar;

    /** 参数 */
    @TableField("param")
    private String param;

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

