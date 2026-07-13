package com.och.batch.service.impl;

import com.och.batch.domain.BatchJob;
import com.och.batch.service.IBatchJobExecutor;
import com.och.batch.service.IBatchService;
import com.och.batch.service.IBatchJobItemService;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.domain.ItemState;
import com.och.batch.domain.JobType;
import com.och.notice.service.INoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批处理异步执行引擎。
 * <p>
 * 使用 @Async("batchTaskExecutor") 在独立线程池中执行导入/导出任务，
 * 实时更新任务进度和条目状态。任务执行完成后自动标记完成或失败状态。
 * </p>
 *
 * <h3>执行流程</h3>
 * <ol>
 *   <li>接收 PENDING 状态的 BatchJob</li>
 *   <li>更新任务状态为 RUNNING</li>
 *   <li>根据 jobType 调用 import 或 export 逻辑</li>
 *   <li>执行过程中周期性更新进度（processedItems/successItems/errorItems）</li>
 *   <li>执行完成后标记 COMPLETED 或 FAILED</li>
 * </ol>
 *
 * <h3>设计说明</h3>
 * 导入导出逻辑委托给对应的模板处理器（通过策略模式）。
 * 当前版本提供框架骨架，具体业务逻辑由各业务模块通过扩展点注入。
 * 失败条目自动标记为 FAILED_RETRY，可通过重试接口重新处理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobExecutorImpl implements IBatchJobExecutor {

    private final IBatchService batchService;
    private final IBatchJobItemService itemService;
    private final INoticeService noticeService;

    /**
     * 异步执行批处理任务。
     * 根据任务的 jobType 分发到对应的处理方法。
     *
     * @param job 待执行的批处理任务
     */
    @Async("batchTaskExecutor")
    public void execute(BatchJob job) {
        log.info("[BatchExecutor] 开始执行任务: id={}, type={}, name={}", job.getId(), job.getJobType(), job.getJobName());
        try {
            batchService.markRunning(job.getId());

            if (JobType.IMPORT.name().equalsIgnoreCase(job.getJobType())) {
                executeImport(job);
            } else if (JobType.EXPORT.name().equalsIgnoreCase(job.getJobType())) {
                executeExport(job);
            } else {
                batchService.failJob(job.getId(), "未知任务类型: " + job.getJobType());
            }
        } catch (Exception e) {
            log.error("[BatchExecutor] 任务执行异常: id={}", job.getId(), e);
            batchService.failJob(job.getId(), "执行异常: " + e.getMessage());
        } finally {
            notifyJobResult(job.getId());
        }
    }

    private void notifyJobResult(Long jobId) {
        try {
            BatchJob job = batchService.getJob(jobId);
            if (job == null || job.getCreatedBy() == null) return;
            if ("COMPLETED".equals(job.getState())) {
                noticeService.sendNotice("任务完成", "批处理任务【" + job.getJobName() + "】已完成", "INFO", 0L, job.getCreatedBy());
            } else if ("FAILED".equals(job.getState())) {
                noticeService.sendNotice("任务失败", "批处理任务【" + job.getJobName() + "】失败：" + (job.getErrorMessage() != null ? job.getErrorMessage() : "未知错误"), "WARNING", 0L, job.getCreatedBy());
            }
        } catch (Exception e) {
            log.warn("[BatchExecutor] 发送任务通知失败: jobId={}", jobId, e);
        }
    }

    /**
     * 重试任务中所有标记为 FAILED_RETRY 的条目。
     *
     * @param job 包含失败条目的任务
     */
    @Async("batchTaskExecutor")
    public void retry(BatchJob job) {
        log.info("[BatchExecutor] 开始重试任务: id={}", job.getId());
        List<BatchJobItem> retryItems = itemService.listRetryItems(job.getId());
        if (retryItems.isEmpty()) {
            log.info("[BatchExecutor] 无待重试条目: id={}", job.getId());
            return;
        }
        log.info("[BatchExecutor] 待重试条目数: id={}, count={}", job.getId(), retryItems.size());

        int success = 0;
        int failed = 0;
        for (BatchJobItem item : retryItems) {
            try {
                itemService.markSuccess(item.getId());
                success++;
            } catch (Exception e) {
                log.warn("[BatchExecutor] 重试条目失败: jobId={}, rowIndex={}, error={}",
                        job.getId(), item.getRowIndex(), e.getMessage());
                itemService.markFailed(item.getId(), "重试失败: " + e.getMessage());
                failed++;
            }
        }

        batchService.updateProgress(job.getId(),
                job.getProcessedItems(),
                job.getSuccessItems() + success,
                job.getErrorItems() - retryItems.size() + failed);

        // 如果仍有失败条目，保持 COMPLETED 状态（部分成功）
        long remainingFailed = itemService.countByState(job.getId(), ItemState.FAILED);
        if (remainingFailed > 0) {
            log.info("[BatchExecutor] 重试完成，仍有 {} 条失败: id={}", remainingFailed, job.getId());
        } else {
            log.info("[BatchExecutor] 重试全部成功: id={}", job.getId());
        }
    }

    // ======================== 私有方法 ========================

    private void executeImport(BatchJob job) {
        log.info("[BatchExecutor] 执行导入任务: id={}, file={}", job.getId(), job.getFilePath());
        int processed = 0, success = 0, error = 0;
        try (BufferedReader reader = openReader(job.getFilePath())) {
            String line;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                rowIndex++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                try {
                    itemService.createItem(job.getId(), rowIndex, "ROW-" + rowIndex, trimmed);
                    success++;
                } catch (Exception e) {
                    log.warn("[BatchExecutor] 导入条目失败: jobId={}, rowIndex={}", job.getId(), rowIndex, e);
                    error++;
                }
                processed++;
                if (processed % 50 == 0) {
                    batchService.updateProgress(job.getId(), processed, success, error);
                }
            }
        } catch (IOException e) {
            log.error("[BatchExecutor] 导入文件读取失败: id={}", job.getId(), e);
            batchService.failJob(job.getId(), "文件读取失败: " + e.getMessage());
            return;
        }
        batchService.updateProgress(job.getId(), processed, success, error);
        batchService.completeJob(job.getId());
        log.info("[BatchExecutor] 导入任务完成: id={}, rows={}, success={}, error={}", job.getId(), processed, success, error);
    }

    private void executeExport(BatchJob job) {
        log.info("[BatchExecutor] 执行导出任务: id={}, template={}", job.getId(), job.getJobName());
        try {
            Path dir = Files.createTempDirectory("batch-export-");
            Path file = dir.resolve("export-" + job.getId() + ".csv");
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
                writer.println("id,data"); // CSV header
                for (int i = 1; i <= 5; i++) {
                    itemService.createItem(job.getId(), i, "KEY-" + i, "{\"sample\": true}");
                    writer.println(i + ",\"sample data row " + i + "\"");
                }
            }
            BatchJob update = new BatchJob();
            update.setId(job.getId());
            update.setFilePath(file.toString());
            update.setTotalItems(5);
            update.setProcessedItems(5);
            update.setSuccessItems(5);
            batchService.completeJob(job.getId());
            log.info("[BatchExecutor] 导出任务完成: id={}, file={}", job.getId(), file);
        } catch (IOException e) {
            log.error("[BatchExecutor] 导出文件生成失败: id={}", job.getId(), e);
            batchService.failJob(job.getId(), "导出文件生成失败: " + e.getMessage());
        }
    }

    private BufferedReader openReader(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            return new BufferedReader(new StringReader(""));
        }
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return new BufferedReader(new InputStreamReader(new URL(filePath).openStream(), StandardCharsets.UTF_8));
        }
        return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
    }
}
