package com.och.report.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI机器人日报实体，对应 report_ai_robot_daily 表。
 * 按天汇总AI机器人会话的统计指标，包括会话数、轮次、延迟、Token消耗和成本。
 */
@Data
@TableName("report_ai_robot_daily")
public class AiRobotDailyReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate reportDate;
    private Integer totalSessions;
    private Integer completedSessions;
    private Integer failedSessions;
    private Integer interruptedSessions;
    private Integer totalTurns;
    private BigDecimal avgTurnsPerSession;
    private Long totalAsrLatencyMs;
    private Long totalTtsLatencyMs;
    private Long totalLlmLatencyMs;
    private Integer avgAsrLatencyMs;
    private Integer avgTtsLatencyMs;
    private Integer avgLlmLatencyMs;
    private Long totalInputTokens;
    private Long totalOutputTokens;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;
}
