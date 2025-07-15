package com.och.system.domain.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月26日 13:41
 */
@Schema
@Data
public class SipAgentStatusVo extends SipAgentVo {


    @Schema(description = "状态时间")
    private Long statusTime;

    @Schema(description = "话后时间")
    private Long callEndTime;

    @Schema(description = "接待人数")
    private Integer receptionNum = 0;

    @Schema(description = "级别",hidden = true)
    private Integer level;

}
