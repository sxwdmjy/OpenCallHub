package com.och.system.domain.query.engine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema
@Data
public class CallEngineAddQuery {

    @Schema(description = "主键ID",hidden = true)
    private Long id;
    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称")
    private String name;


    /**
     * 引擎模块名称
     */
    @NotBlank(message = "引擎模块名称不能为空")
    @Schema(description = "引擎模块名称")
    private String profile;


    /**
     * 引擎类型 1-asr 2-tts
     */
    @Schema(description = "引擎类型 1-asr 2-tts")
    private Integer type;


    /**
     * 服务商 1-阿里 2-腾讯 3-讯飞
     */
    @Schema(description = "服务商 1-阿里 2-腾讯 3-讯飞")
    private Integer providers;


    /**
     * 发声音色
     */
    @Schema(description = "发声音色")
    private String timbre;


    /**
     * asr语法
     */
    @Schema(description = "asr语法")
    private String grammar;


    /**
     * 参数
     */
    @Schema(description = "参数")
    private String param;

}
