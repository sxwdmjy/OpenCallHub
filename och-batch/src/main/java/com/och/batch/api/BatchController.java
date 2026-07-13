package com.och.batch.api;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.batch.service.IBatchService;
import com.och.batch.domain.BatchJob;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.service.IBatchJobItemService;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 批处理管理控制器。
 * <p>
 * 提供批处理任务相关的 REST API 接口，包括：
 * <ul>
 *   <li>分页查询任务列表</li>
 *   <li>查询单个任务详情及进度</li>
 *   <li>创建导入任务（上传文件并异步执行）</li>
 *   <li>创建导出任务（异步生成文件）</li>
 *   <li>更新任务进度</li>
 *   <li>标记任务完成</li>
 *   <li>标记任务失败</li>
 * </ul>
 * 所有接口返回统一的 {@link ApiResponse} 响应体。
 * </p>
 *
 * @author danmo
 */
@Tag(name = "Batch", description = "批处理/导入导出")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final IBatchService batchService;
    private final IBatchJobItemService itemService;

    /**
     * 分页查询批处理任务列表。
     * <p>
     * 按创建时间倒序返回任务列表，支持分页参数。
     * </p>
     *
     * @param pageNum  页码，默认第 1 页
     * @param pageSize 每页大小，默认 20 条
     * @return 分页响应体，包含任务列表、总记录数、页码和页大小
     */
    @Operation(summary = "批处理任务列表")
    @GetMapping("/jobs")
    public ApiResponse<PageResponse<BatchJob>> listJobs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<BatchJob> page = batchService.listJobs(pageNum, pageSize);
        return ApiResponse.success(PageResponse.of(
                page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 根据 ID 查询单个批处理任务详情。
     * <p>
     * 返回任务的完整信息，包括名称、类型、状态、进度（已处理/成功/失败条目数）、
     * 文件路径、错误明细导出 URL 等。
     * </p>
     *
     * @param id 任务 ID
     * @return 批处理任务详情
     */
    @Operation(summary = "查询任务进度")
    @GetMapping("/jobs/{id}")
    public ApiResponse<BatchJob> getJob(@PathVariable Long id) {
        return ApiResponse.success(batchService.getJob(id));
    }

    /**
     * 创建导入任务。
     * <p>
     * 接收导入文件 URL 和模板类型，创建一个类型为 IMPORT 的批处理任务。
     * 任务初始状态为 PENDING，由后台调度器异步执行导入逻辑并更新进度。
     * </p>
     *
     * @param fileUrl      导入文件 URL
     * @param templateType 导入模板类型
     * @return 新创建的批处理任务对象（含自增主键）
     */
    @Operation(summary = "上传导入文件")
    @PostMapping("/import")
    @Audited(action = "IMPORT", targetType = "BATCH", description = "导入数据")
    public ApiResponse<BatchJob> startImport(@RequestParam String fileUrl,
                                              @RequestParam String templateType) {
        BatchJob job = batchService.startImport(fileUrl, templateType);
        return ApiResponse.success(job);
    }

    /**
     * 创建导出任务。
     * <p>
     * 接收模板类型和过滤条件（JSON 格式），创建一个类型为 EXPORT 的批处理任务。
     * 任务初始状态为 PENDING，由后台异步执行数据查询和文件生成。
     * </p>
     *
     * @param templateType 导出模板类型
     * @param filters      导出过滤条件（JSON 格式）
     * @return 新创建的批处理任务对象（含自增主键）
     */
    @Operation(summary = "导出数据")
    @PostMapping("/export")
    @Audited(action = "EXPORT", targetType = "BATCH", description = "导出数据")
    public ApiResponse<BatchJob> startExport(@RequestParam String templateType,
                                              @RequestParam(defaultValue = "{}") String filters) {
        BatchJob job = batchService.startExport(templateType, filters);
        return ApiResponse.success(job);
    }

    /**
     * 更新批处理任务进度。
     * <p>
     * 由后台执行模块回调此接口，实时更新任务的已处理条目数、
     * 成功条目数和失败条目数。
     * </p>
     *
     * @param id             任务 ID
     * @param processedItems 已处理条目数
     * @param successItems   成功条目数
     * @param errorItems     失败条目数
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "更新任务进度")
    @PutMapping("/jobs/{id}/progress")
    public ApiResponse<Void> updateProgress(@PathVariable Long id,
                                             @RequestParam int processedItems,
                                             @RequestParam int successItems,
                                             @RequestParam int errorItems) {
        batchService.updateProgress(id, processedItems, successItems, errorItems);
        return ApiResponse.success();
    }

    /**
     * 标记批处理任务为已完成。
     * <p>
     * 将任务状态更新为 COMPLETED，同时记录完成时间。
     * </p>
     *
     * @param id 任务 ID
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "标记任务完成")
    @PutMapping("/jobs/{id}/complete")
    public ApiResponse<Void> completeJob(@PathVariable Long id) {
        batchService.completeJob(id);
        return ApiResponse.success();
    }

    /**
     * 标记批处理任务为失败。
     * <p>
     * 将任务状态更新为 FAILED，同时记录完成时间和错误信息。
     * </p>
     *
     * @param id           任务 ID
     * @param errorMessage 错误信息描述
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "标记任务失败")
    @PutMapping("/jobs/{id}/fail")
    public ApiResponse<Void> failJob(@PathVariable Long id,
                                      @RequestParam String errorMessage) {
        batchService.failJob(id, errorMessage);
        return ApiResponse.success();
    }

    /**
     * 查询任务条目明细。
     * 分页返回指定任务的所有处理条目，包含成功和失败的明细。
     *
     * @param id       任务ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 条目明细分页列表
     */
    @Operation(summary = "任务条目明细")
    @GetMapping("/jobs/{id}/items")
    public ApiResponse<PageResponse<BatchJobItem>> listItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<BatchJobItem> page = itemService.listItems(id, pageNum, pageSize);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 重试失败条目。
     * 将任务中所有状态为 FAILED 的条目标记为 FAILED_RETRY，然后异步重新处理。
     *
     * @param id 任务ID
     * @return 空响应体，重试已触发
     */
    @Operation(summary = "重试失败条目")
    @PostMapping("/jobs/{id}/retry")
    @Audited(action = "RETRY", targetType = "BATCH_JOB")
    public ApiResponse<Void> retryFailedItems(@PathVariable Long id) {
        batchService.retryFailedItems(id);
        return ApiResponse.success();
    }

    /**
     * 导出错误明细。
     * 生成 CSV 格式的错误明细文本，供客户端下载或保存为文件。
     *
     * @param id 任务ID
     * @return CSV 格式的错误明细字符串
     */
    @Operation(summary = "导出错误明细")
    @GetMapping("/jobs/{id}/errors/export")
    @Audited(action = "EXPORT_ERRORS", targetType = "BATCH_JOB")
    public ApiResponse<String> exportErrors(@PathVariable Long id) {
        String csvContent = batchService.generateErrorFile(id);
        return ApiResponse.success(csvContent);
    }
}
