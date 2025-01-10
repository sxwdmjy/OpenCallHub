package com.och.system.domain.vo.sip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class SipSimpleVo {

    @Schema(description = "sipId")
    private Integer sipId;

    @Schema(description = "sip名称")
    private String sipName;
}
