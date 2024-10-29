package com.och.system.domain.vo.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2024-10-31 14:52
 **/
@Schema
@Data
public class CallSkillAgentRelVo {

    @Schema(description = "主键ID")
    private Long id;
    /**
     *  坐席ID
     */
    @Schema(description = "坐席ID")
    private Long agentId;

    @Schema(description = "坐席名称")
    private String agentName;

    /**
     *  级别
     */
    @Schema(description = "级别")
    private Integer level;
}
