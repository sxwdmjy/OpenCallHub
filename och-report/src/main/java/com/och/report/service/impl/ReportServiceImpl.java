package com.och.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.report.domain.AgentInboundDailyReport;
import com.och.report.domain.AiRobotDailyReport;
import com.och.report.domain.InboundDailyReport;
import com.och.report.domain.OutboundDailyReport;
import com.och.report.domain.QueueDailyReport;
import com.och.report.mapper.AgentInboundDailyReportMapper;
import com.och.report.mapper.AiRobotDailyReportMapper;
import com.och.report.mapper.InboundDailyReportMapper;
import com.och.report.mapper.OutboundDailyReportMapper;
import com.och.report.mapper.QueueDailyReportMapper;
import com.och.report.service.IReportAggregationService;
import com.och.report.service.IReportService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表服务
 * <p>
 * 提供呼入日报和坐席绩效报表等统计查询功能。
 * 当前直接查询 MySQL 数据库做聚合查询，适合中等数据量场景。
 * 后续可迁移至 OLAP 引擎（如 ClickHouse、Apache Doris）以提升大时间范围的查询性能。
 * </p>
 *
 * <h3>查询策略</h3>
 * <ul>
 *   <li>按日期范围查询，使用 MyBatis-Plus LambdaQueryWrapper 构建条件</li>
 *   <li>结果按报表日期倒序排列，最新数据在前</li>
 *   <li>坐席报表支持按坐席 ID 过滤，传 null 查询所有坐席</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final InboundDailyReportMapper inboundReportMapper;
    private final AgentInboundDailyReportMapper agentReportMapper;
    private final OutboundDailyReportMapper outboundReportMapper;
    private final QueueDailyReportMapper queueReportMapper;
    private final AiRobotDailyReportMapper aiRobotReportMapper;
    private final IReportAggregationService aggregationService;

    /**
     * 查询呼入日报统计
     * <p>
     * 按日期范围查询呼入日报汇总数据，包含呼入总量、接通量、未接量、
     * 放弃量、平均振铃时长、平均通话时长、平均等待时长等指标。
     * </p>
     *
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 按日期倒序排列的日报列表，若无数据返回空列表
     */
    public List<InboundDailyReport> getInboundDailyReport(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<InboundDailyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(InboundDailyReport::getReportDate, startDate, endDate);
        wrapper.orderByDesc(InboundDailyReport::getReportDate);
        List<InboundDailyReport> result = inboundReportMapper.selectList(wrapper);
        log.debug("[ReportService] getInboundDailyReport: {} ~ {} -> {} records",
                startDate, endDate, result.size());
        return result;
    }

    /**
     * 查询坐席呼入绩效报表
     * <p>
     * 按坐席 ID 和日期范围查询坐席维度的呼入绩效数据，
     * 包括接听量、未接量、平均通话时长、总通话时长、
     * 话后处理时长、保持次数、转接次数、可用时长等指标。
     * </p>
     *
     * @param agentId   坐席 ID，传 null 表示查询所有坐席
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 按日期倒序排列的坐席绩效报表列表，若无数据返回空列表
     */
    public List<AgentInboundDailyReport> getAgentReport(Long agentId,
                                                         LocalDate startDate,
                                                         LocalDate endDate) {
        LambdaQueryWrapper<AgentInboundDailyReport> wrapper = new LambdaQueryWrapper<>();
        // 按坐席 ID 过滤，为 null 时跳过该条件（查询所有坐席）
        if (agentId != null) {
            wrapper.eq(AgentInboundDailyReport::getAgentId, agentId);
        }
        wrapper.between(AgentInboundDailyReport::getReportDate, startDate, endDate);
        wrapper.orderByDesc(AgentInboundDailyReport::getReportDate);
        List<AgentInboundDailyReport> result = agentReportMapper.selectList(wrapper);
        log.debug("[ReportService] getAgentReport: agentId={}, {} ~ {} -> {} records",
                agentId, startDate, endDate, result.size());
        return result;
    }

    /**
     * 查询外呼日报统计。
     * 按任务ID和日期范围查询外呼日报数据，包含总客户数、拨打次数、接通次数、失败次数、通话时长等。
     *
     * @param taskId    外呼任务ID，传 null 表示查询所有任务
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 按日期倒序排列的外呼日报列表
     */
    public List<OutboundDailyReport> getOutboundDailyReport(Long taskId,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
        LambdaQueryWrapper<OutboundDailyReport> wrapper = new LambdaQueryWrapper<>();
        if (taskId != null) {
            wrapper.eq(OutboundDailyReport::getTaskId, taskId);
        }
        wrapper.between(OutboundDailyReport::getReportDate, startDate, endDate);
        wrapper.orderByDesc(OutboundDailyReport::getReportDate);
        List<OutboundDailyReport> result = outboundReportMapper.selectList(wrapper);
        log.debug("[ReportService] getOutboundDailyReport: taskId={}, {} ~ {} -> {} records",
                taskId, startDate, endDate, result.size());
        return result;
    }

    /**
     * 查询队列SLA日报统计。
     * 按技能组ID和日期范围查询队列SLA日报，包含排队量、应答量、放弃量、SLA达标率等。
     *
     * @param skillGroupId 技能组ID，传 null 表示查询所有技能组
     * @param startDate    开始日期（含）
     * @param endDate      结束日期（含）
     * @return 按日期倒序排列的队列日报列表
     */
    public List<QueueDailyReport> getQueueDailyReport(Long skillGroupId,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {
        LambdaQueryWrapper<QueueDailyReport> wrapper = new LambdaQueryWrapper<>();
        if (skillGroupId != null) {
            wrapper.eq(QueueDailyReport::getSkillGroupId, skillGroupId);
        }
        wrapper.between(QueueDailyReport::getReportDate, startDate, endDate);
        wrapper.orderByDesc(QueueDailyReport::getReportDate);
        List<QueueDailyReport> result = queueReportMapper.selectList(wrapper);
        log.debug("[ReportService] getQueueDailyReport: skillGroupId={}, {} ~ {} -> {} records",
                skillGroupId, startDate, endDate, result.size());
        return result;
    }

    /**
     * 查询AI机器人日报统计。
     * 按日期范围查询AI机器人日报，包含会话数、轮次、延迟、Token消耗和成本。
     *
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 按日期倒序排列的AI机器人日报列表
     */
    public List<AiRobotDailyReport> getAiRobotDailyReport(LocalDate startDate,
                                                           LocalDate endDate) {
        LambdaQueryWrapper<AiRobotDailyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AiRobotDailyReport::getReportDate, startDate, endDate);
        wrapper.orderByDesc(AiRobotDailyReport::getReportDate);
        List<AiRobotDailyReport> result = aiRobotReportMapper.selectList(wrapper);
        log.debug("[ReportService] getAiRobotDailyReport: {} ~ {} -> {} records",
                startDate, endDate, result.size());
        return result;
    }

    /**
     * 手动触发报表重算。
     * 委托给 {@link ReportAggregationService} 执行指定类型和日期范围的聚合重算。
     *
     * @param reportType 报表类型：OUTBOUND、QUEUE、AI_ROBOT、ALL
     * @param startDate  开始日期（含）
     * @param endDate    结束日期（含）
     */
    public void recalculate(String reportType, LocalDate startDate, LocalDate endDate) {
        aggregationService.recalculate(reportType, startDate, endDate);
    }
}
