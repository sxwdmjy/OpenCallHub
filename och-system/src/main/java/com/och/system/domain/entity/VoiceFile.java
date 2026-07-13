package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 语音文件实体
 * <p>映射 voice_file 表，存储语音文件信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("voice_file")
public class VoiceFile {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文件名称 */
    @TableField("name")
    private String name;

    /** 类型：1-手动上传 2-语音合成 */
    @TableField("type")
    private Integer type;

    /** TTS方式：1-腾讯 2-阿里 3-讯飞 */
    @TableField("tts")
    private Integer tts;

    /** 合成文本 */
    @TableField("speech_text")
    private String speechText;

    /** 文件ID */
    @TableField("file_id")
    private Long fileId;

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

