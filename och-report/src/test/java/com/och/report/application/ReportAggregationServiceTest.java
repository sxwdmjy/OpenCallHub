package com.och.report.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.acd.domain.AcdQueueSession;
import com.och.acd.domain.SkillGroup;
import com.och.acd.mapper.AcdQueueSessionMapper;
import com.och.acd.mapper.SkillGroupMapper;
import com.och.ai.domain.AiCostRecord;
import com.och.ai.domain.RobotSession;
import com.och.ai.mapper.AiCostRecordMapper;
import com.och.ai.mapper.RobotSessionMapper;
import com.och.calltask.domain.OutboundDialAttempt;
import com.och.calltask.domain.OutboundTask;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import com.och.calltask.mapper.OutboundTaskMapper;
import com.och.report.domain.AiRobotDailyReport;
import com.och.report.domain.OutboundDailyReport;
import com.och.report.domain.QueueDailyReport;
import com.och.report.mapper.AiRobotDailyReportMapper;
import com.och.report.mapper.OutboundDailyReportMapper;
import com.och.report.mapper.QueueDailyReportMapper;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.junit.jupiter.api.Test;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.mockito.ArgumentCaptor;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.mockito.InjectMocks;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.mockito.Mock;
import com.och.report.service.impl.ReportAggregationServiceImpl;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ReportAggregationService} 单元测试。
 * Mock 所有 10 个 Mapper 依赖，验证聚合逻辑、数据查询和 Upsert 行为。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportAggregationService 单元测试")
class ReportAggregationServiceTest {

    @Mock private OutboundDailyReportMapper outboundReportMapper;
    @Mock private QueueDailyReportMapper queueReportMapper;
    @Mock private AiRobotDailyReportMapper aiRobotReportMapper;
    @Mock private AcdQueueSessionMapper queueSessionMapper;
    @Mock private SkillGroupMapper skillGroupMapper;
    @Mock private OutboundTaskMapper outboundTaskMapper;
    @Mock private OutboundDialAttemptMapper dialAttemptMapper;
    @Mock private RobotSessionMapper robotSessionMapper;
    @Mock private AiCostRecordMapper aiCostRecordMapper;

    @InjectMocks
    private ReportAggregationServiceImpl aggregationService;

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 7, 7);

    // ======================== aggregateOutboundDaily ========================

    @Test
    @DisplayName("aggregateOutboundDaily: 无外呼任务时跳过聚合")
    void aggregateOutboundDaily_shouldSkipWhenNoTasks() {
        when(outboundTaskMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        aggregationService.aggregateOutboundDaily(TEST_DATE);

        verify(outboundTaskMapper).selectList(any());
        verify(dialAttemptMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("aggregateOutboundDaily: 有任务和拨号记录时正确聚合")
    void aggregateOutboundDaily_shouldAggregateCorrectly() {
        OutboundTask task = new OutboundTask();
        task.setId(1L);
        task.setTaskName("测试任务");
        task.setTotalCustomers(100);
        task.setCompletedCustomers(50);
        task.setConnectedCount(30);
        when(outboundTaskMapper.selectList(any())).thenReturn(List.of(task));

        OutboundDialAttempt a1 = new OutboundDialAttempt();
        a1.setDialResult("ANSWERED");
        a1.setDurationSeconds(120);
        OutboundDialAttempt a2 = new OutboundDialAttempt();
        a2.setDialResult("ANSWERED");
        a2.setDurationSeconds(60);
        OutboundDialAttempt a3 = new OutboundDialAttempt();
        a3.setDialResult("NO_ANSWER");
        when(dialAttemptMapper.selectList(any())).thenReturn(List.of(a1, a2, a3));

        when(outboundReportMapper.selectOne(any())).thenReturn(null);
        when(outboundReportMapper.insert(any())).thenReturn(1);

        aggregationService.aggregateOutboundDaily(TEST_DATE);

        ArgumentCaptor<OutboundDailyReport> captor = ArgumentCaptor.forClass(OutboundDailyReport.class);
        verify(outboundReportMapper).insert(captor.capture());
        OutboundDailyReport report = captor.getValue();

        assertThat(report.getReportDate()).isEqualTo(TEST_DATE);
        assertThat(report.getTaskId()).isEqualTo(1L);
        assertThat(report.getTotalAttempts()).isEqualTo(3);
        assertThat(report.getAnsweredAttempts()).isEqualTo(2);
        assertThat(report.getFailedAttempts()).isEqualTo(1);
        assertThat(report.getTotalDurationSeconds()).isEqualTo(180);
    }

    @Test
    @DisplayName("aggregateOutboundDaily: 已存在记录时更新而非插入")
    void aggregateOutboundDaily_shouldUpdateWhenRecordExists() {
        OutboundTask task = new OutboundTask();
        task.setId(1L);
        task.setTaskName("测试任务");
        when(outboundTaskMapper.selectList(any())).thenReturn(List.of(task));
        when(dialAttemptMapper.selectList(any())).thenReturn(Collections.emptyList());

        OutboundDailyReport existing = new OutboundDailyReport();
        existing.setId(100L);
        when(outboundReportMapper.selectOne(any())).thenReturn(existing);
        when(outboundReportMapper.updateById(any())).thenReturn(1);

        aggregationService.aggregateOutboundDaily(TEST_DATE);

        ArgumentCaptor<OutboundDailyReport> captor = ArgumentCaptor.forClass(OutboundDailyReport.class);
        verify(outboundReportMapper).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(100L);
    }

    // ======================== aggregateQueueDaily ========================

    @Test
    @DisplayName("aggregateQueueDaily: 无技能组时跳过聚合")
    void aggregateQueueDaily_shouldSkipWhenNoGroups() {
        when(skillGroupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        aggregationService.aggregateQueueDaily(TEST_DATE);

        verify(queueSessionMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("aggregateQueueDaily: 正确聚合SLA指标")
    void aggregateQueueDaily_shouldAggregateSlaMetrics() {
        SkillGroup group = new SkillGroup();
        group.setId(1L);
        group.setGroupName("客服组");
        group.setMaxWaitSeconds(30);
        group.setEnabled(true);
        when(skillGroupMapper.selectList(any())).thenReturn(List.of(group));

        AcdQueueSession s1 = new AcdQueueSession();
        s1.setAssignedAgentId(10L);
        s1.setWaitSeconds(20);
        s1.setEndCause("ANSWERED");
        AcdQueueSession s2 = new AcdQueueSession();
        s2.setAssignedAgentId(11L);
        s2.setWaitSeconds(40);
        s2.setEndCause("ANSWERED");
        AcdQueueSession s3 = new AcdQueueSession();
        s3.setEndCause("ABANDONED");
        when(queueSessionMapper.selectList(any())).thenReturn(List.of(s1, s2, s3));

        when(queueReportMapper.selectOne(any())).thenReturn(null);
        when(queueReportMapper.insert(any())).thenReturn(1);

        aggregationService.aggregateQueueDaily(TEST_DATE);

        ArgumentCaptor<QueueDailyReport> captor = ArgumentCaptor.forClass(QueueDailyReport.class);
        verify(queueReportMapper).insert(captor.capture());
        QueueDailyReport report = captor.getValue();

        assertThat(report.getTotalQueued()).isEqualTo(3);
        assertThat(report.getTotalAnswered()).isEqualTo(2);
        assertThat(report.getTotalAbandoned()).isEqualTo(1);
        assertThat(report.getMaxWaitSeconds()).isEqualTo(40);
        assertThat(report.getSlaTargetSeconds()).isEqualTo(30);
        assertThat(report.getSlaMetCount()).isEqualTo(1); // 只有 s1 的 20s <= 30s
    }

    // ======================== aggregateAiRobotDaily ========================

    @Test
    @DisplayName("aggregateAiRobotDaily: 无会话时填充空报表")
    void aggregateAiRobotDaily_shouldProduceEmptyReportWhenNoSessions() {
        when(robotSessionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiCostRecordMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiRobotReportMapper.selectOne(any())).thenReturn(null);
        when(aiRobotReportMapper.insert(any())).thenReturn(1);

        aggregationService.aggregateAiRobotDaily(TEST_DATE);

        ArgumentCaptor<AiRobotDailyReport> captor = ArgumentCaptor.forClass(AiRobotDailyReport.class);
        verify(aiRobotReportMapper).insert(captor.capture());
        AiRobotDailyReport report = captor.getValue();
        assertThat(report.getTotalSessions()).isEqualTo(0);
    }

    @Test
    @DisplayName("aggregateAiRobotDaily: 正确聚合会话统计和成本")
    void aggregateAiRobotDaily_shouldAggregateSessionsAndCost() {
        RobotSession s1 = new RobotSession();
        s1.setEndReason("HANGUP");
        s1.setTurnCount(5);
        s1.setTotalAsrLatencyMs(1000);
        s1.setTotalTtsLatencyMs(2000);
        s1.setTotalLlmLatencyMs(3000);
        RobotSession s2 = new RobotSession();
        s2.setEndReason("ERROR");
        s2.setTurnCount(3);
        s2.setTotalAsrLatencyMs(800);
        s2.setTotalTtsLatencyMs(1500);
        s2.setTotalLlmLatencyMs(2500);
        when(robotSessionMapper.selectList(any())).thenReturn(List.of(s1, s2));

        AiCostRecord c1 = new AiCostRecord();
        c1.setInputTokens(100);
        c1.setOutputTokens(200);
        c1.setCost(0.05);
        AiCostRecord c2 = new AiCostRecord();
        c2.setInputTokens(300);
        c2.setOutputTokens(400);
        c2.setCost(0.10);
        when(aiCostRecordMapper.selectList(any())).thenReturn(List.of(c1, c2));

        when(aiRobotReportMapper.selectOne(any())).thenReturn(null);
        when(aiRobotReportMapper.insert(any())).thenReturn(1);

        aggregationService.aggregateAiRobotDaily(TEST_DATE);

        ArgumentCaptor<AiRobotDailyReport> captor = ArgumentCaptor.forClass(AiRobotDailyReport.class);
        verify(aiRobotReportMapper).insert(captor.capture());
        AiRobotDailyReport report = captor.getValue();

        assertThat(report.getTotalSessions()).isEqualTo(2);
        assertThat(report.getCompletedSessions()).isEqualTo(1);
        assertThat(report.getFailedSessions()).isEqualTo(1);
        assertThat(report.getTotalTurns()).isEqualTo(8);
        assertThat(report.getTotalAsrLatencyMs()).isEqualTo(1800);
        assertThat(report.getTotalTtsLatencyMs()).isEqualTo(3500);
        assertThat(report.getTotalLlmLatencyMs()).isEqualTo(5500);
        assertThat(report.getTotalInputTokens()).isEqualTo(400);
        assertThat(report.getTotalOutputTokens()).isEqualTo(600);
    }

    // ======================== recalculate ========================

    @Test
    @DisplayName("recalculate: ALL 类型触发所有聚合")
    void recalculate_shouldAggregateAllTypes() {
        OutboundTask task = new OutboundTask();
        task.setId(1L);
        task.setTaskName("test");
        when(outboundTaskMapper.selectList(any())).thenReturn(List.of(task));
        when(dialAttemptMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(outboundReportMapper.selectOne(any())).thenReturn(null);
        when(outboundReportMapper.insert(any())).thenReturn(1);

        SkillGroup group = new SkillGroup();
        group.setId(1L);
        group.setGroupName("test");
        group.setEnabled(true);
        when(skillGroupMapper.selectList(any())).thenReturn(List.of(group));
        when(queueSessionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(queueReportMapper.selectOne(any())).thenReturn(null);
        when(queueReportMapper.insert(any())).thenReturn(1);

        when(robotSessionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiCostRecordMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(aiRobotReportMapper.selectOne(any())).thenReturn(null);
        when(aiRobotReportMapper.insert(any())).thenReturn(1);

        aggregationService.recalculate("ALL", TEST_DATE, TEST_DATE);

        verify(outboundReportMapper).insert(any());
        verify(queueReportMapper).insert(any());
        verify(aiRobotReportMapper).insert(any());
    }
}
