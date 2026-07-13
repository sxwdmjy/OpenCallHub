package com.och.report.api;

import lombok.RequiredArgsConstructor;

import com.och.batch.domain.BatchJob;
import com.och.batch.service.IBatchService;
import com.och.common.api.ApiResponse;
import com.och.report.application.RecalculationRequest;
import com.och.report.service.IReportService;
import com.och.report.domain.AgentInboundDailyReport;
import com.och.report.domain.AiRobotDailyReport;
import com.och.report.domain.InboundDailyReport;
import com.och.report.domain.OutboundDailyReport;
import com.och.report.domain.QueueDailyReport;
import org.springframework.security.access.prepost.PreAuthorize;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表查询控制器。
 * <p>
 * 提供呼入日报和坐席呼入绩效日报的查询接口。
 * 所有业务逻辑委托给 {@link ReportService} 处理，
 * 控制器仅负责参数校验、格式转换和统一响应封装。
 * </p>
 */
@Tag(name = "Report", description = "报表查询")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    /** 报表服务，封装所有报表统计查询逻辑 */
    private final IReportService reportService;
    private final IBatchService batchService;

    /**
     * 呼入日报查询
     * <p>
     * 按日期范围查询呼入日报汇总数据。日期格式为 ISO 标准（yyyy-MM-dd）。
     * 若未传日期范围，默认查询最近 7 天。
     * </p>
     *
     * @param startDate 开始日期，格式 yyyy-MM-dd，可选（默认 7 天前）
     * @param endDate   结束日期，格式 yyyy-MM-dd，可选（默认今天）
     * @return 呼入日报列表，按日期倒序排列
     */
    @Operation(summary = "呼入日报")
    @GetMapping("/inbound/daily")
    public ApiResponse<List<InboundDailyReport>> getInboundDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // 未传日期时使用默认值：最近 7 天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<InboundDailyReport> reports = reportService.getInboundDailyReport(startDate, endDate);
        return ApiResponse.success(reports);
    }

    /**
     * 坐席呼入绩效日报查询
     * <p>
     * 按坐席 ID 和日期范围查询坐席维度的绩效日报。
     * 传 agentId=null 时查询所有坐席。
     * </p>
     *
     * @param agentId   坐席 ID，可选（null 表示查询所有坐席）
     * @param startDate 开始日期，格式 yyyy-MM-dd，可选（默认 7 天前）
     * @param endDate   结束日期，格式 yyyy-MM-dd，可选（默认今天）
     * @return 坐席绩效日报列表，按日期倒序排列
     */
    @Operation(summary = "坐席呼入日报")
    @GetMapping("/agent-inbound/daily")
    public ApiResponse<List<AgentInboundDailyReport>> getAgentInboundDaily(
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // 未传日期时使用默认值：最近 7 天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<AgentInboundDailyReport> reports = reportService.getAgentReport(agentId, startDate, endDate);
        return ApiResponse.success(reports);
    }

    /**
     * 外呼日报查询。
     * 按任务ID和日期范围查询外呼日报，包含拨打次数、接通次数、失败次数、通话时长等。
     *
     * @param taskId    外呼任务ID，可选
     * @param startDate 开始日期，可选（默认 7 天前）
     * @param endDate   结束日期，可选（默认今天）
     * @return 外呼日报列表，按日期倒序排列
     */
    @Operation(summary = "外呼日报")
    @GetMapping("/outbound/daily")
    public ApiResponse<List<OutboundDailyReport>> getOutboundDaily(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<OutboundDailyReport> reports = reportService.getOutboundDailyReport(taskId, startDate, endDate);
        return ApiResponse.success(reports);
    }

    /**
     * 队列SLA日报查询。
     * 按技能组ID和日期范围查询队列日报，包含排队量、应答量、放弃量、SLA达标率等。
     *
     * @param skillGroupId 技能组ID，可选
     * @param startDate    开始日期，可选（默认 7 天前）
     * @param endDate      结束日期，可选（默认今天）
     * @return 队列日报列表，按日期倒序排列
     */
    @Operation(summary = "队列SLA日报")
    @GetMapping("/queue/daily")
    public ApiResponse<List<QueueDailyReport>> getQueueDaily(
            @RequestParam(required = false) Long skillGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<QueueDailyReport> reports = reportService.getQueueDailyReport(skillGroupId, startDate, endDate);
        return ApiResponse.success(reports);
    }

    /**
     * AI机器人日报查询。
     * 按日期范围查询AI机器人日报，包含会话数、轮次、延迟、Token消耗和成本。
     *
     * @param startDate 开始日期，可选（默认 7 天前）
     * @param endDate   结束日期，可选（默认今天）
     * @return AI机器人日报列表，按日期倒序排列
     */
    @Operation(summary = "AI机器人日报")
    @GetMapping("/ai-robot/daily")
    public ApiResponse<List<AiRobotDailyReport>> getAiRobotDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<AiRobotDailyReport> reports = reportService.getAiRobotDailyReport(startDate, endDate);
        return ApiResponse.success(reports);
    }

    /**
     * 手动触发报表重算。
     * 支持指定报表类型（OUTBOUND/QUEUE/AI_ROBOT/ALL）和日期范围进行批量重算。
     * 用于数据修复或首次上线时的历史数据回填。
     *
     * @param request 重算请求，含报表类型和日期范围
     * @return 空响应体，表示重算已触发
     */
    @Operation(summary = "手动触发报表重算")
    @PostMapping("/recalculate")
    @Audited(action = "RECALCULATE", targetType = "REPORT")
    public ApiResponse<Void> recalculate(@RequestBody RecalculationRequest request) {
        if (request.getStartDate() == null) {
            request.setStartDate(LocalDate.now().minusDays(1));
        }
        if (request.getEndDate() == null) {
            request.setEndDate(LocalDate.now().minusDays(1));
        }
        if (request.getReportType() == null || request.getReportType().isBlank()) {
            request.setReportType("ALL");
        }
        reportService.recalculate(request.getReportType(), request.getStartDate(), request.getEndDate());
        return ApiResponse.success();
    }

    @Operation(summary = "导出报表")
    @PreAuthorize("@authz.hasPerm('report:export')")
    @PostMapping("/export")
    @Audited(action = "EXPORT_REPORT", targetType = "REPORT")
    public ApiResponse<BatchJob> exportReport(@RequestParam String reportType,
                                               @RequestParam(defaultValue = "{}") String filters) {
        BatchJob job = batchService.startExport(reportType, filters);
        return ApiResponse.success(job);
    }
}
