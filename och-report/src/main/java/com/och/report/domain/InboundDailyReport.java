package com.och.report.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 呼入日报实体，对应 report_inbound_daily 表。
 * 按天汇总呼入通话的关键统计指标。
 */
@Data
@TableName("report_inbound_daily")
public class InboundDailyReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate reportDate;
    private Integer totalCalls;
    private Integer answeredCalls;
    private Integer missedCalls;
    private Integer abandonedCalls;
    private Integer voicemailCalls;
    private BigDecimal avgRingSeconds;
    private BigDecimal avgTalkSeconds;
    private BigDecimal avgWaitSeconds;
    private BigDecimal avgAfterCallSeconds;
    private Integer totalHoldCount;
    private Integer totalTransferCount;
    private LocalDateTime createdAt;
}
