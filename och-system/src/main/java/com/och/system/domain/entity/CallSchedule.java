package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.och.system.domain.query.schedule.CallScheduleAddQuery;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 日程安排实体
 * <p>映射 call_schedule 表，存储日程安排信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_schedule")
public class CallSchedule {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 日程名称 */
    @TableField("name")
    private String name;

    /** 优先级：0-10 */
    @TableField("level")
    private Integer level;

    /** 类型：0-指定时间 1-相对时间 */
    @TableField("type")
    private Integer type;

    /** 开始日期 */
    @TableField("start_day")
    private String startDay;

    /** 结束日期 */
    @TableField("end_day")
    private String endDay;

    /** 开始时间 */
    @TableField("start_time")
    private String startTime;

    /** 结束时间 */
    @TableField("end_time")
    private String endTime;

    /** 周期时间 */
    @TableField("work_cycle")
    private String workCycle;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /** 逻辑删除：0-正常 1-已删除 */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

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

