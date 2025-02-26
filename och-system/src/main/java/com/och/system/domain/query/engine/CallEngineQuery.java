package com.och.system.domain.query.engine;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema
@Data
public class CallEngineQuery extends BaseQuery {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "ID",hidden = true)
    private List<Long> ids;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "引擎模块名称")
    private String profile;

    @Schema(description = "引擎类型 1-asr 2-tts")
    private Integer type;

    @Schema(description = "服务商 1-阿里 2-腾讯 3-讯飞")
    private Integer providers;
}
