package com.och.report.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 坐席呼入日报实体，对应 report_agent_inbound_daily 表。
 * 按天、按坐席汇总呼入通话的绩效统计指标。
 */
@Data
@TableName("report_agent_inbound_daily")
public class AgentInboundDailyReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate reportDate;
    private Long agentId;
    private Integer answeredCalls;
    private Integer missedCalls;
    private BigDecimal avgTalkSeconds;
    private Integer totalTalkSeconds;
    private BigDecimal avgAfterCallSeconds;
    private Integer holdCount;
    private Integer transferCount;
    private Integer availableSeconds;
    private LocalDateTime createdAt;
}
