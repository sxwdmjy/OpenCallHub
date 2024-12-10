package com.och.system.domain.vo.callin;

import com.och.system.domain.entity.CallSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2024-11-10 13:32
 **/
@Schema
@Data
public class CallInPhoneRelVo {

    /**
     * 主键
     */

    @Schema(description = "主键")
    private Long id;

    /**
     * 呼入号码ID
     */
    @Schema(description = "呼入号码ID")
    private Long callInId;


    /**
     * 日程ID
     */
    @Schema(description = "日程ID")
    private Long scheduleId;

    /**
     * 日程名称
     */
    @Schema(description = "日程名称")
    private String scheduleName;

    @Schema(description = "日程详情",hidden = true)
    private CallSchedule scheduleDetail;

    /**
     * 路由类型 1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr
     */
    @Schema(description = "路由类型 1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr")
    private Integer routeType;


    /**
     * 路由类型值
     */
    @Schema(description = "路由类型值")
    private String routeValue;


}
