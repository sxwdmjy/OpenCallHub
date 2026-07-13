package com.och.report.service.impl;

import com.och.report.service.IReportAggregationScheduler;
import com.och.report.service.IReportAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "och.report.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ReportAggregationSchedulerImpl implements IReportAggregationScheduler {

    private final IReportAggregationService aggregationService;

    @Scheduled(cron = "0 5 0 * * ?")
    public void aggregateDailyReports() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[ReportScheduler] 定时聚合任务开始: date={}", yesterday);
        try { aggregationService.aggregateOutboundDaily(yesterday); } catch (Exception e) { log.error("[ReportScheduler] 外呼日报聚合失败: date={}", yesterday, e); }
        try { aggregationService.aggregateQueueDaily(yesterday); } catch (Exception e) { log.error("[ReportScheduler] 队列日报聚合失败: date={}", yesterday, e); }
        try { aggregationService.aggregateAiRobotDaily(yesterday); } catch (Exception e) { log.error("[ReportScheduler] AI机器人日报聚合失败: date={}", yesterday, e); }
        log.info("[ReportScheduler] 定时聚合任务结束: date={}", yesterday);
    }
}
