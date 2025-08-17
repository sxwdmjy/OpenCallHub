package com.och.system.domain.query.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * @author danmo
 * @since 2024-08-09 17:14:50
 **/
@Schema
@Data
public class CallScheduleAddQuery {
    /**
     * 主键
     */
    @Schema(description = "主键",hidden = true)
    private Long id;


    /**
     * 日程名称
     */
    @NotEmpty(message = "名称不能为空")
    @Schema(description = "日程名称",requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;


    /**
     * 优先级 0-10
     */
    @Schema(description = "优先级 0-10",requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(0)
    @Max(10)
    private Integer level;


    /**
     * 0-指定时间 1-相对时间
     */
    @Schema(description = "0-指定时间 1-相对时间",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类型不能为空")
    private Integer type;


    /**
     * 开始日期
     */
    @Schema(description = "开始日期",requiredMode = Schema.RequiredMode.REQUIRED)
    private String startDay;


    /**
     * 结束日期
     */
    @Schema(description = "结束日期",requiredMode = Schema.RequiredMode.REQUIRED)
    private String endDay;


    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String startTime;


    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endTime;

    /**
     * 周期时间
     */
    @Schema(description = "周期时间")
    private String workCycle;
}
