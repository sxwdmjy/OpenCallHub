package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import com.och.system.domain.query.file.VoiceFileAddQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 语音文件表(VoiceFile)表实体类
 *
 * @author danmo
 * @since 2023-11-01 14:34:24
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("voice_file")
public class VoiceFile extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 873281745058075384L;

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    @TableField("name")
    private String name;


    /**
     * 类型 1-手动上传 2-语音合成
     */
    @Schema(description = "类型 1-手动上传 2-语音合成")
    @TableField("type")
    private Integer type;


    /**
     * tts方式 1-腾讯 2-阿里 3-讯飞(type=9生效)
     */
    @Schema(description = "tts方式 1-腾讯 2-阿里 3-讯飞(type=2生效)")
    @TableField("tts")
    private Integer tts;


    /**
     * 合成文本
     */
    @Schema(description = "合成文本")
    @TableField("speech_text")
    private String speechText;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    @TableField("file_id")
    private Long fileId;



    public void setQuery2Entity(VoiceFileAddQuery query) {
        this.id = query.getId();
        this.name = query.getName();
        this.type = query.getType();
        this.tts = query.getTts();
        this.speechText = query.getSpeechText();
        this.fileId = query.getFileId();
    }
}

