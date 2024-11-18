package com.och.system.domain.query.file;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023-11-01 16:13
 **/
@Schema
@Data
public class VoiceFileQuery extends BaseQuery {
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "主键ID列表", hidden = true)
    private List<Long> ids;

    /**
     *  文件名称
     */
    @Schema(description = "文件名称")
    private String name;

    /**
     *  类型 1-本地存储 2-腾讯云 3-阿里云 9-语音合成
     */
    @NotEmpty(message = "类型不能为空")
    @Schema(description = "类型 1-本地存储 2-腾讯云 3-阿里云 9-语音合成")
    private Integer type;


    /**
     *  tts方式 1-腾讯 2-阿里 3-讯飞(type=9生效)
     */
    @Schema(description = "tts方式 1-腾讯 2-阿里 3-讯飞(type=9生效)")
    private Integer tts;

}
