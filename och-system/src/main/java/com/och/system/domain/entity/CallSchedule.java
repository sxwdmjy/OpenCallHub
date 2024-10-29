package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import com.och.system.domain.query.schedule.CallScheduleAddQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * 日程安排表(CallSchedule)表实体类
 *
 * @author danmo
 * @since 2024-08-09 17:14:50
 */
@EqualsAndHashCode(callSuper = false)
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_schedule")
public class CallSchedule extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -83484748238063743L;

    /**
     * 主键
     */

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 日程名称
     */
    @Schema(description = "日程名称")
    @TableField("name")
    private String name;


    /**
     * 优先级 0-10
     */
    @Schema(description = "优先级 0-10")
    @TableField("level")
    private Integer level;


    /**
     * 0-指定时间 1-相对时间
     */
    @Schema(description = "0-指定时间 1-相对时间")
    @TableField("type")
    private Integer type;


    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    @TableField("start_day")
    private String startDay;


    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    @TableField("end_day")
    private String endDay;


    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    @TableField("start_time")
    private String startTime;


    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    @TableField("end_time")
    private String endTime;


    /**
     * 周期时间
     */
    @Schema(description = "周期时间")
    @TableField("work_cycle")
    private String workCycle;




    public void setQuery2Entity(CallScheduleAddQuery query) {
        this.id = query.getId();
        this.name = query.getName();
        this.level = query.getLevel();
        this.type = query.getType();
        this.startDay = query.getStartDay();
        this.endDay = query.getEndDay();
        this.startTime = query.getStartTime();
        this.endTime = query.getEndTime();
        this.workCycle = query.getWorkCycle();
    }
}

