package com.och.report.api;

import com.och.common.api.ApiResponse;
import com.och.report.application.RecalculationRequest;
import com.och.report.service.IReportService;
import com.och.report.domain.AgentInboundDailyReport;
import com.och.report.domain.AiRobotDailyReport;
import com.och.report.domain.InboundDailyReport;
import com.och.report.domain.OutboundDailyReport;
import com.och.report.domain.QueueDailyReport;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ReportController} 单元测试。
 * <p>
 * Mock {@link ReportService}，验证控制器参数处理、默认值逻辑和响应封装。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController 单元测试")
class ReportControllerTest {

    @Mock
    private IReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private static final LocalDate START_DATE = LocalDate.of(2026, 6, 10);
    private static final LocalDate END_DATE = LocalDate.of(2026, 6, 16);

    private InboundDailyReport sampleInboundReport;
    private AgentInboundDailyReport sampleAgentReport;

    @BeforeEach
    void setUp() {
        sampleInboundReport = new InboundDailyReport();
        sampleInboundReport.setId(1L);
        sampleInboundReport.setReportDate(LocalDate.of(2026, 6, 15));
        sampleInboundReport.setTotalCalls(100);
        sampleInboundReport.setAnsweredCalls(85);

        sampleAgentReport = new AgentInboundDailyReport();
        sampleAgentReport.setId(1L);
        sampleAgentReport.setReportDate(LocalDate.of(2026, 6, 15));
        sampleAgentReport.setAgentId(1001L);
        sampleAgentReport.setAnsweredCalls(45);
        sampleAgentReport.setAvgTalkSeconds(new BigDecimal("95.0"));
    }

    // ==================== getInboundDaily ====================

    @Test
    @DisplayName("传入明确日期范围时，返回成功响应并包含呼入日报数据")
    void getInboundDaily_shouldReturnSuccessResponseWithData() {
        // given
        when(reportService.getInboundDailyReport(START_DATE, END_DATE))
                .thenReturn(List.of(sampleInboundReport));

        // when
        ApiResponse<List<InboundDailyReport>> response =
                reportController.getInboundDaily(START_DATE, END_DATE);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getTotalCalls()).isEqualTo(100);
        verify(reportService).getInboundDailyReport(START_DATE, END_DATE);
    }

    @Test
    @DisplayName("未传日期时默认查询最近7天并返回成功响应")
    void getInboundDaily_shouldUseDefaultDateRangeWhenDatesNotProvided() {
        // given
        when(reportService.getInboundDailyReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(sampleInboundReport));

        // when
        ApiResponse<List<InboundDailyReport>> response =
                reportController.getInboundDaily(null, null);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getInboundDailyReport(startCaptor.capture(), endCaptor.capture());

        LocalDate today = LocalDate.now();
        // 开始日期应为 7 天前
        assertThat(startCaptor.getValue()).isEqualTo(today.minusDays(7));
        // 结束日期应为今天
        assertThat(endCaptor.getValue()).isEqualTo(today);
    }

    @Test
    @DisplayName("查询无数据时返回空的成功响应")
    void getInboundDaily_shouldReturnEmptySuccessResponseWhenNoData() {
        // given
        when(reportService.getInboundDailyReport(START_DATE, END_DATE))
                .thenReturn(Collections.emptyList());

        // when
        ApiResponse<List<InboundDailyReport>> response =
                reportController.getInboundDaily(START_DATE, END_DATE);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEmpty();
        verify(reportService).getInboundDailyReport(START_DATE, END_DATE);
    }

    // ==================== getAgentInboundDaily ====================

    @Test
    @DisplayName("传入坐席ID和日期范围时，返回该坐席的绩效数据")
    void getAgentInboundDaily_shouldReturnAgentDataWhenAgentIdProvided() {
        // given
        Long agentId = 1001L;
        when(reportService.getAgentReport(eq(agentId), eq(START_DATE), eq(END_DATE)))
                .thenReturn(List.of(sampleAgentReport));

        // when
        ApiResponse<List<AgentInboundDailyReport>> response =
                reportController.getAgentInboundDaily(agentId, START_DATE, END_DATE);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getAgentId()).isEqualTo(1001L);
        assertThat(response.getData().get(0).getAnsweredCalls()).isEqualTo(45);
        verify(reportService).getAgentReport(agentId, START_DATE, END_DATE);
    }

    @Test
    @DisplayName("不传坐席ID时传递 null 给 Service，返回所有坐席数据")
    void getAgentInboundDaily_shouldQueryAllAgentsWhenAgentIdNotProvided() {
        // given
        AgentInboundDailyReport report2 = new AgentInboundDailyReport();
        report2.setAgentId(1002L);
        report2.setAnsweredCalls(30);

        when(reportService.getAgentReport(isNull(), eq(START_DATE), eq(END_DATE)))
                .thenReturn(List.of(sampleAgentReport, report2));

        // when
        ApiResponse<List<AgentInboundDailyReport>> response =
                reportController.getAgentInboundDaily(null, START_DATE, END_DATE);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(2);
        verify(reportService).getAgentReport(isNull(), eq(START_DATE), eq(END_DATE));
    }

    @Test
    @DisplayName("坐席查询未传日期时，使用默认7天范围")
    void getAgentInboundDaily_shouldUseDefaultDateRangeWhenDatesNotProvided() {
        // given
        when(reportService.getAgentReport(any(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(sampleAgentReport));

        // when
        ApiResponse<List<AgentInboundDailyReport>> response =
                reportController.getAgentInboundDaily(1001L, null, null);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getAgentReport(eq(1001L), startCaptor.capture(), endCaptor.capture());

        LocalDate today = LocalDate.now();
        assertThat(startCaptor.getValue()).isEqualTo(today.minusDays(7));
        assertThat(endCaptor.getValue()).isEqualTo(today);
    }

    // ==================== getOutboundDaily ====================

    @Test
    @DisplayName("外呼日报：传入任务ID和日期范围时返回数据")
    void getOutboundDaily_shouldReturnOutboundData() {
        OutboundDailyReport report = new OutboundDailyReport();
        report.setTaskId(1L);
        report.setTaskName("任务A");
        report.setTotalAttempts(50);
        when(reportService.getOutboundDailyReport(eq(1L), eq(START_DATE), eq(END_DATE)))
                .thenReturn(List.of(report));

        ApiResponse<List<OutboundDailyReport>> response =
                reportController.getOutboundDaily(1L, START_DATE, END_DATE);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getTaskId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("外呼日报：未传日期时使用默认7天范围")
    void getOutboundDaily_shouldUseDefaultDateRangeWhenDatesNull() {
        when(reportService.getOutboundDailyReport(isNull(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        reportController.getOutboundDaily(null, null, null);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getOutboundDailyReport(isNull(), startCaptor.capture(), endCaptor.capture());

        LocalDate today = LocalDate.now();
        assertThat(startCaptor.getValue()).isEqualTo(today.minusDays(7));
        assertThat(endCaptor.getValue()).isEqualTo(today);
    }

    // ==================== getQueueDaily ====================

    @Test
    @DisplayName("队列日报：传入技能组ID和日期范围时返回数据")
    void getQueueDaily_shouldReturnQueueSlaData() {
        QueueDailyReport report = new QueueDailyReport();
        report.setSkillGroupId(1L);
        report.setTotalAnswered(80);
        when(reportService.getQueueDailyReport(eq(1L), eq(START_DATE), eq(END_DATE)))
                .thenReturn(List.of(report));

        ApiResponse<List<QueueDailyReport>> response =
                reportController.getQueueDaily(1L, START_DATE, END_DATE);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getTotalAnswered()).isEqualTo(80);
    }

    @Test
    @DisplayName("队列日报：不传技能组ID查询所有技能组")
    void getQueueDaily_shouldQueryAllGroupsWhenSkillGroupIdNull() {
        when(reportService.getQueueDailyReport(isNull(), eq(START_DATE), eq(END_DATE)))
                .thenReturn(Collections.emptyList());

        ApiResponse<List<QueueDailyReport>> response =
                reportController.getQueueDaily(null, START_DATE, END_DATE);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEmpty();
    }

    // ==================== getAiRobotDaily ====================

    @Test
    @DisplayName("AI机器人日报：传入日期范围时返回数据")
    void getAiRobotDaily_shouldReturnAiRobotData() {
        AiRobotDailyReport report = new AiRobotDailyReport();
        report.setTotalSessions(10);
        report.setTotalCost(new java.math.BigDecimal("5.50"));
        when(reportService.getAiRobotDailyReport(START_DATE, END_DATE))
                .thenReturn(List.of(report));

        ApiResponse<List<AiRobotDailyReport>> response =
                reportController.getAiRobotDaily(START_DATE, END_DATE);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getTotalSessions()).isEqualTo(10);
    }

    @Test
    @DisplayName("AI机器人日报：未传日期时使用默认7天范围")
    void getAiRobotDaily_shouldUseDefaultDateRangeWhenDatesNull() {
        when(reportService.getAiRobotDailyReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        reportController.getAiRobotDaily(null, null);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getAiRobotDailyReport(startCaptor.capture(), endCaptor.capture());

        LocalDate today = LocalDate.now();
        assertThat(startCaptor.getValue()).isEqualTo(today.minusDays(7));
        assertThat(endCaptor.getValue()).isEqualTo(today);
    }

    // ==================== recalculate ====================

    @Test
    @DisplayName("重算：传入有效请求时返回成功")
    void recalculate_shouldReturnSuccessForValidRequest() {
        RecalculationRequest request = new RecalculationRequest();
        request.setReportType("OUTBOUND");
        request.setStartDate(START_DATE);
        request.setEndDate(END_DATE);

        ApiResponse<Void> response = reportController.recalculate(request);

        assertThat(response.getCode()).isEqualTo(200);
        verify(reportService).recalculate("OUTBOUND", START_DATE, END_DATE);
    }

    @Test
    @DisplayName("重算：未传类型和日期时使用默认值ALL和前一天")
    void recalculate_shouldUseDefaultsWhenFieldsNull() {
        RecalculationRequest request = new RecalculationRequest();

        reportController.recalculate(request);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        verify(reportService).recalculate("ALL", yesterday, yesterday);
    }
}
