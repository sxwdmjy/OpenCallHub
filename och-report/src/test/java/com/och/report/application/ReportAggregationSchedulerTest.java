package com.och.report.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.och.report.service.IReportAggregationScheduler;
import com.och.report.service.IReportAggregationService;
import com.och.report.service.impl.ReportAggregationSchedulerImpl;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ReportAggregationScheduler} 单元测试。
 * 验证定时任务的调度逻辑、异常隔离和日期计算。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportAggregationScheduler 单元测试")
class ReportAggregationSchedulerTest {

    @Mock
    private IReportAggregationService aggregationService;

    @InjectMocks
    private ReportAggregationSchedulerImpl scheduler;

    @Test
    @DisplayName("aggregateDailyReports: 触发前一天的三种聚合")
    void shouldAggregateYesterdayForAllTypes() {
        scheduler.aggregateDailyReports();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        verify(aggregationService).aggregateOutboundDaily(yesterday);
        verify(aggregationService).aggregateQueueDaily(yesterday);
        verify(aggregationService).aggregateAiRobotDaily(yesterday);
    }

    @Test
    @DisplayName("aggregateDailyReports: 外呼聚合失败不影响队列和AI聚合")
    void shouldContinueWhenOutboundFails() {
        doThrow(new RuntimeException("外呼聚合异常"))
                .when(aggregationService).aggregateOutboundDaily(any());

        scheduler.aggregateDailyReports();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        verify(aggregationService).aggregateOutboundDaily(yesterday);
        verify(aggregationService).aggregateQueueDaily(yesterday);
        verify(aggregationService).aggregateAiRobotDaily(yesterday);
    }

    @Test
    @DisplayName("aggregateDailyReports: 队列聚合失败不影响其他")
    void shouldContinueWhenQueueFails() {
        doThrow(new RuntimeException("队列聚合异常"))
                .when(aggregationService).aggregateQueueDaily(any());

        scheduler.aggregateDailyReports();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        verify(aggregationService).aggregateOutboundDaily(yesterday);
        verify(aggregationService).aggregateQueueDaily(yesterday);
        verify(aggregationService).aggregateAiRobotDaily(yesterday);
    }

    @Test
    @DisplayName("aggregateDailyReports: 全部聚合失败不抛异常")
    void shouldNotThrowWhenAllFail() {
        doThrow(new RuntimeException("异常"))
                .when(aggregationService).aggregateOutboundDaily(any());
        doThrow(new RuntimeException("异常"))
                .when(aggregationService).aggregateQueueDaily(any());
        doThrow(new RuntimeException("异常"))
                .when(aggregationService).aggregateAiRobotDaily(any());

        // 不应抛出异常
        scheduler.aggregateDailyReports();

        verify(aggregationService).aggregateOutboundDaily(any());
        verify(aggregationService).aggregateQueueDaily(any());
        verify(aggregationService).aggregateAiRobotDaily(any());
    }
}
