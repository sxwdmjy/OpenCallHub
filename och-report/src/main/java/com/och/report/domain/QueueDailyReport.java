package com.och.report.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 队列日报实体，对应 report_queue_daily 表。
 * 按天、按技能组汇总排队和SLA统计指标。
 */
@Data
@TableName("report_queue_daily")
public class QueueDailyReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate reportDate;
    private Long skillGroupId;
    private String skillGroupName;
    private Integer totalQueued;
    private Integer totalAnswered;
    private Integer totalAbandoned;
    private Integer totalTimeout;
    private BigDecimal avgWaitSeconds;
    private Integer maxWaitSeconds;
    private Integer slaTargetSeconds;
    private Integer slaMetCount;
    private BigDecimal slaRate;
    private LocalDateTime createdAt;
}
