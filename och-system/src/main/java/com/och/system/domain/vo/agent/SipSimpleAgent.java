package com.och.system.domain.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月26日 13:41
 */
@Schema
@Data
public class SipSimpleAgent {

    @Schema(description = "坐席ID")
    private Long agentId;

    @Schema(description = "坐席名称")
    private String name;
}
