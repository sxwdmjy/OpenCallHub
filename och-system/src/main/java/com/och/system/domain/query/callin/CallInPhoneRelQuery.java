package com.och.system.domain.query.callin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2024-11-09 18:18
 **/
@Schema
@Data
public class CallInPhoneRelQuery {

    /**
     * 呼入号码ID
     */
    @Schema(description = "呼入号码ID",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long callInId;

    /**
     * 日程ID
     */
    @Schema(description = "日程ID",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long scheduleId;

    /**
     * 路由类型 1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr
     */
    @Schema(description = "路由类型 1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer routeType;

    /**
     * 路由类型值
     */
    @Schema(description = "路由类型值")
    private String routeValue;

    
}
