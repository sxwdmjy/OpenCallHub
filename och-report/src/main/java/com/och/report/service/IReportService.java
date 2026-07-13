package com.och.report.service;
import com.och.report.domain.*;
import java.time.LocalDate;
import java.util.List;
public interface IReportService {
    List<InboundDailyReport> getInboundDailyReport(LocalDate startDate, LocalDate endDate);
    List<AgentInboundDailyReport> getAgentReport(Long agentId, LocalDate startDate, LocalDate endDate);
    List<OutboundDailyReport> getOutboundDailyReport(Long taskId, LocalDate startDate, LocalDate endDate);
    List<QueueDailyReport> getQueueDailyReport(Long skillGroupId, LocalDate startDate, LocalDate endDate);
    List<AiRobotDailyReport> getAiRobotDailyReport(LocalDate startDate, LocalDate endDate);
    void recalculate(String reportType, LocalDate startDate, LocalDate endDate);
}
