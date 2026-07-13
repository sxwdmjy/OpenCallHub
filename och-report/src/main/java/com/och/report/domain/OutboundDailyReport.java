package com.och.report.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 外呼日报实体，对应 report_outbound_daily 表。
 * 按天、按外呼任务汇总拨打统计指标。
 */
@Data
@TableName("report_outbound_daily")
public class OutboundDailyReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate reportDate;
    private Long taskId;
    private String taskName;
    private Integer totalCustomers;
    private Integer completedCustomers;
    private Integer connectedCount;
    private Integer totalAttempts;
    private Integer answeredAttempts;
    private Integer failedAttempts;
    private Integer totalDurationSeconds;
    private BigDecimal avgDurationSeconds;
    private LocalDateTime createdAt;
}
