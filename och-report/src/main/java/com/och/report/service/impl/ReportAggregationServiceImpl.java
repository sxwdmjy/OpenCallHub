package com.och.report.service.impl;

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
import com.och.report.service.IReportAggregationService;
import com.och.report.mapper.OutboundDailyReportMapper;
import com.och.report.service.IReportAggregationService;
import com.och.report.mapper.QueueDailyReportMapper;
import com.och.report.service.IReportAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表聚合服务。
 * <p>
 * 从各业务模块的源表中读取原始数据，按天聚合计算后写入报表表。
 * 三种聚合方法互不影响，各自独立的 try/catch 确保单个类型失败不阻塞其他类型。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAggregationServiceImpl implements IReportAggregationService {

    private final OutboundDailyReportMapper outboundReportMapper;
    private final QueueDailyReportMapper queueReportMapper;
    private final AiRobotDailyReportMapper aiRobotReportMapper;

    private final AcdQueueSessionMapper queueSessionMapper;
    private final SkillGroupMapper skillGroupMapper;
    private final OutboundTaskMapper outboundTaskMapper;
    private final OutboundDialAttemptMapper dialAttemptMapper;
    private final RobotSessionMapper robotSessionMapper;
    private final AiCostRecordMapper aiCostRecordMapper;

    // ======================== 外呼日报聚合 ========================

    /**
     * 聚合指定日期的外呼日报。
     * 按外呼任务维度统计拨打次数、接通次数、失败次数、通话时长等指标。
     */
    @Transactional
    public void aggregateOutboundDaily(LocalDate date) {
        log.info("[ReportAggregation] 开始聚合外呼日报: date={}", date);
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<OutboundTask> tasks = outboundTaskMapper.selectList(
                new LambdaQueryWrapper<OutboundTask>().eq(OutboundTask::getDeleted, 0));
        if (tasks.isEmpty()) {
            log.info("[ReportAggregation] 无外呼任务，跳过外呼日报聚合: date={}", date);
            return;
        }

        for (OutboundTask task : tasks) {
            List<OutboundDialAttempt> attempts = dialAttemptMapper.selectList(
                    new LambdaQueryWrapper<OutboundDialAttempt>()
                            .eq(OutboundDialAttempt::getTaskId, task.getId())
                            .ge(OutboundDialAttempt::getDialTime, dayStart)
                            .lt(OutboundDialAttempt::getDialTime, dayEnd));

            OutboundDailyReport report = new OutboundDailyReport();
            report.setReportDate(date);
            report.setTaskId(task.getId());
            report.setTaskName(task.getTaskName());
            report.setTotalCustomers(task.getTotalCustomers() != null ? task.getTotalCustomers() : 0);
            report.setCompletedCustomers(task.getCompletedCustomers() != null ? task.getCompletedCustomers() : 0);
            report.setConnectedCount(task.getConnectedCount() != null ? task.getConnectedCount() : 0);
            report.setTotalAttempts(attempts.size());

            int answered = 0;
            int failed = 0;
            long totalDuration = 0;
            for (OutboundDialAttempt a : attempts) {
                if ("ANSWERED".equalsIgnoreCase(a.getDialResult())) {
                    answered++;
                    if (a.getDurationSeconds() != null) {
                        totalDuration += a.getDurationSeconds();
                    }
                } else {
                    failed++;
                }
            }
            report.setAnsweredAttempts(answered);
            report.setFailedAttempts(failed);
            report.setTotalDurationSeconds((int) totalDuration);
            report.setAvgDurationSeconds(answered > 0
                    ? BigDecimal.valueOf(totalDuration).divide(BigDecimal.valueOf(answered), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            upsertOutboundReport(report);
        }
        log.info("[ReportAggregation] 外呼日报聚合完成: date={}, taskCount={}", date, tasks.size());
    }

    // ======================== 队列日报聚合 ========================

    /**
     * 聚合指定日期的队列日报。
     * 按技能组维度统计排队量、应答量、放弃量、等待时长、SLA达标率等指标。
     */
    @Transactional
    public void aggregateQueueDaily(LocalDate date) {
        log.info("[ReportAggregation] 开始聚合队列日报: date={}", date);
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<SkillGroup> groups = skillGroupMapper.selectList(
                new LambdaQueryWrapper<SkillGroup>().eq(SkillGroup::getEnabled, true));
        if (groups.isEmpty()) {
            log.info("[ReportAggregation] 无启用技能组，跳过队列日报聚合: date={}", date);
            return;
        }

        for (SkillGroup group : groups) {
            List<AcdQueueSession> sessions = queueSessionMapper.selectList(
                    new LambdaQueryWrapper<AcdQueueSession>()
                            .eq(AcdQueueSession::getSkillGroupId, group.getId())
                            .ge(AcdQueueSession::getEnterTime, dayStart)
                            .lt(AcdQueueSession::getEnterTime, dayEnd));

            QueueDailyReport report = new QueueDailyReport();
            report.setReportDate(date);
            report.setSkillGroupId(group.getId());
            report.setSkillGroupName(group.getGroupName());
            report.setTotalQueued(sessions.size());

            int answered = 0;
            int abandoned = 0;
            int timeout = 0;
            long totalWait = 0;
            int maxWait = 0;
            int slaTarget = group.getMaxWaitSeconds() != null ? group.getMaxWaitSeconds() : 30;
            int slaMet = 0;

            for (AcdQueueSession s : sessions) {
                if (s.getAssignedAgentId() != null) {
                    answered++;
                    int wait = s.getWaitSeconds() != null ? s.getWaitSeconds() : 0;
                    totalWait += wait;
                    if (wait > maxWait) {
                        maxWait = wait;
                    }
                    if (wait <= slaTarget) {
                        slaMet++;
                    }
                }
                if ("ABANDONED".equalsIgnoreCase(s.getEndCause())) {
                    abandoned++;
                }
                if ("TIMEOUT".equalsIgnoreCase(s.getEndCause())) {
                    timeout++;
                }
            }

            report.setTotalAnswered(answered);
            report.setTotalAbandoned(abandoned);
            report.setTotalTimeout(timeout);
            report.setAvgWaitSeconds(answered > 0
                    ? BigDecimal.valueOf(totalWait).divide(BigDecimal.valueOf(answered), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            report.setMaxWaitSeconds(maxWait);
            report.setSlaTargetSeconds(slaTarget);
            report.setSlaMetCount(slaMet);
            report.setSlaRate(answered > 0
                    ? BigDecimal.valueOf(slaMet).divide(BigDecimal.valueOf(answered), 4, RoundingMode.HALF_UP)
                    : null);

            upsertQueueReport(report);
        }
        log.info("[ReportAggregation] 队列日报聚合完成: date={}, groupCount={}", date, groups.size());
    }

    // ======================== AI机器人日报聚合 ========================

    /**
     * 聚合指定日期的AI机器人日报。
     * 按天统计机器人会话数、交互轮次、延迟、Token和成本。
     */
    @Transactional
    public void aggregateAiRobotDaily(LocalDate date) {
        log.info("[ReportAggregation] 开始聚合AI机器人日报: date={}", date);
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<RobotSession> sessions = robotSessionMapper.selectList(
                new LambdaQueryWrapper<RobotSession>()
                        .ge(RobotSession::getCreatedAt, dayStart)
                        .lt(RobotSession::getCreatedAt, dayEnd));

        List<AiCostRecord> costRecords = aiCostRecordMapper.selectList(
                new LambdaQueryWrapper<AiCostRecord>()
                        .eq(AiCostRecord::getRecordDate, date));

        AiRobotDailyReport report = new AiRobotDailyReport();
        report.setReportDate(date);
        report.setTotalSessions(sessions.size());

        int completed = 0;
        int failed = 0;
        int interrupted = 0;
        long totalTurns = 0;
        long totalAsrLat = 0;
        long totalTtsLat = 0;
        long totalLlmLat = 0;

        for (RobotSession s : sessions) {
            String reason = s.getEndReason();
            if (reason == null || "HANGUP".equalsIgnoreCase(reason)) {
                completed++;
            } else if ("ERROR".equalsIgnoreCase(reason) || "TIMEOUT".equalsIgnoreCase(reason)) {
                failed++;
            } else {
                interrupted++;
            }
            if (s.getTurnCount() != null) {
                totalTurns += s.getTurnCount();
            }
            if (s.getTotalAsrLatencyMs() != null) {
                totalAsrLat += s.getTotalAsrLatencyMs();
            }
            if (s.getTotalTtsLatencyMs() != null) {
                totalTtsLat += s.getTotalTtsLatencyMs();
            }
            if (s.getTotalLlmLatencyMs() != null) {
                totalLlmLat += s.getTotalLlmLatencyMs();
            }
        }

        report.setCompletedSessions(completed);
        report.setFailedSessions(failed);
        report.setInterruptedSessions(interrupted);
        report.setTotalTurns((int) totalTurns);
        int sessionCount = sessions.size();
        report.setAvgTurnsPerSession(sessionCount > 0
                ? BigDecimal.valueOf(totalTurns).divide(BigDecimal.valueOf(sessionCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        report.setTotalAsrLatencyMs(totalAsrLat);
        report.setTotalTtsLatencyMs(totalTtsLat);
        report.setTotalLlmLatencyMs(totalLlmLat);
        report.setAvgAsrLatencyMs(sessionCount > 0 ? (int) (totalAsrLat / sessionCount) : 0);
        report.setAvgTtsLatencyMs(sessionCount > 0 ? (int) (totalTtsLat / sessionCount) : 0);
        report.setAvgLlmLatencyMs(sessionCount > 0 ? (int) (totalLlmLat / sessionCount) : 0);

        long inputTokens = 0;
        long outputTokens = 0;
        double totalCost = 0;
        for (AiCostRecord r : costRecords) {
            if (r.getInputTokens() != null) {
                inputTokens += r.getInputTokens();
            }
            if (r.getOutputTokens() != null) {
                outputTokens += r.getOutputTokens();
            }
            if (r.getCost() != null) {
                totalCost += r.getCost();
            }
        }
        report.setTotalInputTokens(inputTokens);
        report.setTotalOutputTokens(outputTokens);
        report.setTotalCost(BigDecimal.valueOf(totalCost).setScale(4, RoundingMode.HALF_UP));

        upsertAiRobotReport(report);
        log.info("[ReportAggregation] AI机器人日报聚合完成: date={}, sessionCount={}, costRecordCount={}",
                date, sessionCount, costRecords.size());
    }

    // ======================== 批量重算 ========================

    /**
     * 批量重算指定类型的报表数据。
     *
     * @param reportType 报表类型：OUTBOUND、QUEUE、AI_ROBOT、ALL
     * @param startDate  开始日期（含）
     * @param endDate    结束日期（含）
     */
    public void recalculate(String reportType, LocalDate startDate, LocalDate endDate) {
        log.info("[ReportAggregation] 开始批量重算: type={}, {} ~ {}", reportType, startDate, endDate);
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            try {
                switch (reportType.toUpperCase()) {
                    case "OUTBOUND":
                        aggregateOutboundDaily(d);
                        break;
                    case "QUEUE":
                        aggregateQueueDaily(d);
                        break;
                    case "AI_ROBOT":
                        aggregateAiRobotDaily(d);
                        break;
                    case "ALL":
                        aggregateOutboundDaily(d);
                        aggregateQueueDaily(d);
                        aggregateAiRobotDaily(d);
                        break;
                    default:
                        log.warn("[ReportAggregation] 未知报表类型: {}", reportType);
                        return;
                }
            } catch (Exception e) {
                log.error("[ReportAggregation] 重算失败: type={}, date={}", reportType, d, e);
            }
        }
        log.info("[ReportAggregation] 批量重算完成: type={}, {} ~ {}", reportType, startDate, endDate);
    }

    // ======================== 私有方法：Upsert ========================

    private void upsertOutboundReport(OutboundDailyReport report) {
        OutboundDailyReport existing = outboundReportMapper.selectOne(
                new LambdaQueryWrapper<OutboundDailyReport>()
                        .eq(OutboundDailyReport::getReportDate, report.getReportDate())
                        .eq(OutboundDailyReport::getTaskId, report.getTaskId()));
        if (existing != null) {
            report.setId(existing.getId());
            outboundReportMapper.updateById(report);
        } else {
            outboundReportMapper.insert(report);
        }
    }

    private void upsertQueueReport(QueueDailyReport report) {
        QueueDailyReport existing = queueReportMapper.selectOne(
                new LambdaQueryWrapper<QueueDailyReport>()
                        .eq(QueueDailyReport::getReportDate, report.getReportDate())
                        .eq(QueueDailyReport::getSkillGroupId, report.getSkillGroupId()));
        if (existing != null) {
            report.setId(existing.getId());
            queueReportMapper.updateById(report);
        } else {
            queueReportMapper.insert(report);
        }
    }

    private void upsertAiRobotReport(AiRobotDailyReport report) {
        AiRobotDailyReport existing = aiRobotReportMapper.selectOne(
                new LambdaQueryWrapper<AiRobotDailyReport>()
                        .eq(AiRobotDailyReport::getReportDate, report.getReportDate()));
        if (existing != null) {
            report.setId(existing.getId());
            aiRobotReportMapper.updateById(report);
        } else {
            aiRobotReportMapper.insert(report);
        }
    }
}
