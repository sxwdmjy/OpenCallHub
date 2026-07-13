package com.och.report.application;

import lombok.Data;

import java.time.LocalDate;

/**
 * 报表重算请求 DTO。
 * 用于手动触发指定类型的报表重算，支持按日期范围批量重算。
 */
@Data
public class RecalculationRequest {

    /** 报表类型：OUTBOUND、QUEUE、AI_ROBOT、ALL */
    private String reportType;

    /** 开始日期（含） */
    private LocalDate startDate;

    /** 结束日期（含） */
    private LocalDate endDate;
}
