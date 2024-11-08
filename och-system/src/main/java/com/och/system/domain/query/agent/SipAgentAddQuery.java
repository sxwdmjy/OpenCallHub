package com.och.system.domain.query.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月23日 11:03
 */
@Schema
@Data
public class SipAgentAddQuery {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", hidden = true)
    private Long id;

    /**
     * 坐席名称
     */
    @Schema(description = "坐席名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 主键ID
     */
    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;


    /**
     * 坐席账号
     */
    @NotEmpty(message = "坐席账号不能为空")
    @Schema(description = "坐席账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String agentNumber;


    /**
     * 状态 0-未开通 1-开通
     */
    @Schema(description = "状态 0-未开通 1-开通")
    private Integer status;

}
