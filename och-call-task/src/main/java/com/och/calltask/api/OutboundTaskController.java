package com.och.calltask.api;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.calltask.application.*;
import com.och.calltask.service.IOutboundTaskService;
import com.och.calltask.service.IOutboundFilterService;
import com.och.calltask.service.IPreviewDialingService;
import com.och.calltask.domain.OutboundDialAttempt;
import com.och.calltask.domain.OutboundTask;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 外呼任务管理控制器
 * <p>
 * 提供外呼任务的全生命周期管理 API，包括任务 CRUD、状态流转、客户过滤、
 * 预览式拨号（领取/释放/拨打）等功能。
 * </p>
 */
@Tag(name = "OutboundTask", description = "外呼任务管理")
@RestController
@RequestMapping("/api/outbound/tasks")
@RequiredArgsConstructor
public class OutboundTaskController {

    private final IOutboundTaskService taskService;
    private final IOutboundFilterService filterService;
    private final IPreviewDialingService previewService;

    @GetMapping
    public ApiResponse<PageResponse<OutboundTask>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<OutboundTask> page = taskService.listTasks(pageNum, pageSize, keyword);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OutboundTask> get(@PathVariable Long id) {
        return ApiResponse.success(taskService.getTask(id));
    }

    @PostMapping
    @Audited(action = "CREATE", targetType = "OUTBOUND_TASK")
    public ApiResponse<OutboundTask> create(@RequestBody OutboundTask task) {
        return ApiResponse.success(taskService.createTask(task));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<OutboundTask> submit(@PathVariable Long id) {
        return ApiResponse.success(taskService.submitTask(id));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<OutboundTask> start(@PathVariable Long id) {
        return ApiResponse.success(taskService.startTask(id));
    }

    @PostMapping("/{id}/pause")
    public ApiResponse<OutboundTask> pause(@PathVariable Long id) {
        return ApiResponse.success(taskService.pauseTask(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OutboundTask> cancel(@PathVariable Long id) {
        return ApiResponse.success(taskService.cancelTask(id));
    }

    @PostMapping("/{id}/filter")
    public ApiResponse<Void> applyFilters(@PathVariable Long id) {
        filterService.applyFilters(id);
        return ApiResponse.success();
    }

    @Operation(summary = "预览式外呼: 领取客户")
    @PostMapping("/{taskId}/claim")
    public ApiResponse<?> claimCustomer(@PathVariable Long taskId, @RequestParam Long agentId) {
        return ApiResponse.success(previewService.claimCustomer(taskId, agentId));
    }

    @Operation(summary = "预览式外呼: 释放客户")
    @PostMapping("/{taskId}/release/{customerId}")
    public ApiResponse<Void> releaseCustomer(@PathVariable Long taskId, @PathVariable Long customerId,
                                              @RequestParam Long agentId) {
        previewService.releaseCustomer(customerId, agentId);
        return ApiResponse.success();
    }

    @Operation(summary = "预览式外呼: 开始拨打")
    @PostMapping("/{taskId}/dial/{customerId}")
    public ApiResponse<OutboundDialAttempt> startDial(@PathVariable Long taskId, @PathVariable Long customerId,
                                                       @RequestParam Long agentId) {
        return ApiResponse.success(previewService.startDial(taskId, customerId, agentId));
    }
}
