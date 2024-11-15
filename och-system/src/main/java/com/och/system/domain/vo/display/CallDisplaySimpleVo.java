package com.och.system.domain.vo.display;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月26日 13:44
 */
@Schema
@Data
public class CallDisplaySimpleVo {

    @Schema(description = "显号ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long displayId;

    @Schema(description = "显号号码")
    private String displayNumber;
}
