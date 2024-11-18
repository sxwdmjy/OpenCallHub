package com.och.system.domain.vo.file;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2023-11-02 11:43
 **/
@Schema
@Data
public class VoiceFileVo extends BaseVo {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    private Long id;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String name;


    /**
     * 类型 1-本地存储 2-腾讯云 3-阿里云 9-语音合成
     */
    @Schema(description = "类型 1-本地存储 2-腾讯云 3-阿里云 9-语音合成")
    private Integer type;


    /**
     * tts方式 1-腾讯 2-阿里 3-讯飞(type=9生效)
     */
    @Schema(description = "tts方式 1-腾讯 2-阿里 3-讯飞(type=9生效)")
    private Integer tts;


    /**
     * 合成文本
     */
    @Schema(description = "合成文本")
    private String speechText;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private String fileId;
    /**
     * 文件地址
     */
    @Schema(description = "文件地址")
    private String file;

}
