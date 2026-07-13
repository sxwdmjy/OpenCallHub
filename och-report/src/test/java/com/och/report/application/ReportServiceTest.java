package com.och.report.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.report.domain.*;
import com.och.report.mapper.*;
import com.och.report.service.IReportAggregationService;
import com.och.report.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 单元测试")
class ReportServiceTest {

    @Mock private InboundDailyReportMapper inboundReportMapper;
    @Mock private AgentInboundDailyReportMapper agentReportMapper;
    @Mock private OutboundDailyReportMapper outboundReportMapper;
    @Mock private QueueDailyReportMapper queueReportMapper;
    @Mock private AiRobotDailyReportMapper aiRobotReportMapper;
    @Mock private IReportAggregationService aggregationService;

    @InjectMocks private ReportServiceImpl reportService;

    private static final LocalDate START_DATE = LocalDate.of(2026, 6, 10);
    private static final LocalDate END_DATE = LocalDate.of(2026, 6, 16);
    private InboundDailyReport sampleInboundReport;
    private AgentInboundDailyReport sampleAgentReport;

    @BeforeEach
    void setUp() {
        sampleInboundReport = new InboundDailyReport();
        sampleInboundReport.setId(1L); sampleInboundReport.setReportDate(LocalDate.of(2026, 6, 15));
        sampleInboundReport.setTotalCalls(100); sampleInboundReport.setAnsweredCalls(85);
        sampleAgentReport = new AgentInboundDailyReport();
        sampleAgentReport.setId(1L); sampleAgentReport.setReportDate(LocalDate.of(2026, 6, 15));
        sampleAgentReport.setAgentId(1001L); sampleAgentReport.setAnsweredCalls(45);
    }

    @Test @DisplayName("getInboundDailyReport: 按日期范围查询返回结果")
    void getInboundDailyReport_shouldReturnResults() {
        when(inboundReportMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleInboundReport));
        List<InboundDailyReport> result = reportService.getInboundDailyReport(START_DATE, END_DATE);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalCalls()).isEqualTo(100);
    }

    @Test @DisplayName("getInboundDailyReport: 无数据返回空列表")
    void getInboundDailyReport_shouldReturnEmptyWhenNoData() {
        when(inboundReportMapper.selectList(any())).thenReturn(Collections.emptyList());
        assertThat(reportService.getInboundDailyReport(START_DATE, END_DATE)).isEmpty();
    }

    @Test @DisplayName("getAgentReport: 按坐席ID过滤")
    void getAgentReport_shouldFilterByAgentId() {
        when(agentReportMapper.selectList(any())).thenReturn(List.of(sampleAgentReport));
        List<AgentInboundDailyReport> result = reportService.getAgentReport(1001L, START_DATE, END_DATE);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAgentId()).isEqualTo(1001L);
    }

    @Test @DisplayName("getAgentReport: agentId为null时不过滤")
    void getAgentReport_shouldNotFilterWhenNull() {
        when(agentReportMapper.selectList(any())).thenReturn(Collections.emptyList());
        assertThat(reportService.getAgentReport(null, START_DATE, END_DATE)).isEmpty();
    }

    @Test @DisplayName("getOutboundDailyReport: 按任务ID查询")
    void getOutboundDailyReport_shouldFilterByTaskId() {
        OutboundDailyReport report = new OutboundDailyReport();
        report.setTaskId(1L); report.setTotalAttempts(50);
        when(outboundReportMapper.selectList(any())).thenReturn(List.of(report));
        assertThat(reportService.getOutboundDailyReport(1L, START_DATE, END_DATE)).hasSize(1);
    }

    @Test @DisplayName("getQueueDailyReport: 按技能组查询SLA")
    void getQueueDailyReport_shouldFilterBySkillGroup() {
        QueueDailyReport report = new QueueDailyReport();
        report.setSkillGroupId(1L); report.setTotalAnswered(80);
        when(queueReportMapper.selectList(any())).thenReturn(List.of(report));
        assertThat(reportService.getQueueDailyReport(1L, START_DATE, END_DATE).get(0).getTotalAnswered()).isEqualTo(80);
    }

    @Test @DisplayName("getAiRobotDailyReport: 按日期范围查询")
    void getAiRobotDailyReport_shouldReturnData() {
        AiRobotDailyReport report = new AiRobotDailyReport();
        report.setTotalSessions(10);
        when(aiRobotReportMapper.selectList(any())).thenReturn(List.of(report));
        assertThat(reportService.getAiRobotDailyReport(START_DATE, END_DATE).get(0).getTotalSessions()).isEqualTo(10);
    }

    @Test @DisplayName("recalculate: 委托给AggregationService")
    void recalculate_shouldDelegateToAggregationService() {
        reportService.recalculate("OUTBOUND", START_DATE, START_DATE);
        verify(aggregationService).recalculate("OUTBOUND", START_DATE, START_DATE);
    }
}
