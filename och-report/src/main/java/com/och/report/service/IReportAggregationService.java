package com.och.report.service;
import java.time.LocalDate;
public interface IReportAggregationService {
    void aggregateOutboundDaily(LocalDate date);
    void aggregateQueueDaily(LocalDate date);
    void aggregateAiRobotDaily(LocalDate date);
    void recalculate(String reportType, LocalDate startDate, LocalDate endDate);
}
