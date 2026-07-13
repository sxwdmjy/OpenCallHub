package com.och.batch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.och.batch.domain.BatchJob;
import com.och.batch.service.IBatchService;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.service.IBatchJobExecutor;
import com.och.batch.service.IBatchJobItemService;
import com.och.batch.service.IBatchService;
import com.och.batch.domain.JobState;
import com.och.batch.service.IBatchService;
import com.och.batch.domain.JobType;
import com.och.batch.service.IBatchService;
import com.och.batch.mapper.BatchJobMapper;
import com.och.batch.service.IBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批处理任务服务实现。
 * <p>继承 MyBatis-Plus ServiceImpl 获得批量操作和分页能力。</p>
 */
@Slf4j
@Service
public class BatchServiceImpl extends ServiceImpl<BatchJobMapper, BatchJob> implements IBatchService {

    private final IBatchJobItemService itemService;
    private final IBatchJobExecutor batchJobExecutor;

    public BatchServiceImpl(IBatchJobItemService itemService, IBatchJobExecutor batchJobExecutor) {
        this.itemService = itemService;
        this.batchJobExecutor = batchJobExecutor;
    }

    /**
     * 分页查询批处理任务列表。
     * <p>
     * 按创建时间倒序排列，确保最新的任务排在最前面。
     * 返回 MyBatis-Plus 分页对象，包含总记录数和当前页数据。
     * </p>
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页查询结果
     */
    public IPage<BatchJob> listJobs(int pageNum, int pageSize) {
        log.info("[BatchService] 分页查询批处理任务列表: pageNum={}, pageSize={}", pageNum, pageSize);
        LambdaQueryWrapper<BatchJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BatchJob::getCreatedAt);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 根据 ID 获取单个批处理任务详情。
     * <p>
     * 从数据库中查询指定 ID 的任务记录，包含当前进度信息
     * （totalItems、processedItems、successItems、errorItems 等）。
     * </p>
     *
     * @param id 任务 ID
     * @return 批处理任务对象，若不存在则返回 null
     */
    public BatchJob getJob(Long id) {
        log.info("[BatchService] 查询批处理任务详情: id={}", id);
        return baseMapper.selectById(id);
    }

    /**
     * 创建导入任务。
     * <p>
     * 根据指定的文件 URL 和导入模板类型创建一个新的导入任务，
     * 任务类型为 IMPORT，初始状态设为 PENDING（待处理）。
     * 任务持久化到数据库后，由调度器或消费者异步执行实际的导入逻辑。
     * </p>
     *
     * @param fileUrl      导入文件 URL
     * @param templateType 导入模板类型
     * @return 新创建的批处理任务对象（含自增主键）
     */
    @Transactional
    public BatchJob startImport(String fileUrl, String templateType) {
        log.info("[BatchService] 创建导入任务: fileUrl={}, templateType={}", fileUrl, templateType);
        BatchJob job = new BatchJob();
        job.setJobName(templateType + "导入 - " + LocalDateTime.now().toString());
        job.setJobType(JobType.IMPORT.name());
        job.setState(JobState.PENDING.name());
        job.setFilePath(fileUrl);
        job.setTotalItems(0);
        job.setProcessedItems(0);
        job.setSuccessItems(0);
        job.setErrorItems(0);
        baseMapper.insert(job);
        log.info("[BatchService] 导入任务已创建: id={}, jobName={}", job.getId(), job.getJobName());
        return job;
    }

    /**
     * 创建导出任务。
     * <p>
     * 根据指定的模板类型和过滤条件创建一个新的导出任务，
     * 任务类型为 EXPORT，初始状态设为 PENDING（待处理）。
     * 任务持久化后由后台异步执行数据查询和文件生成。
     * </p>
     *
     * @param templateType 导出模板类型
     * @param filters      导出过滤条件（JSON 或查询参数格式）
     * @return 新创建的批处理任务对象（含自增主键）
     */
    @Transactional
    public BatchJob startExport(String templateType, String filters) {
        log.info("[BatchService] 创建导出任务: templateType={}, filters={}", templateType, filters);
        BatchJob job = new BatchJob();
        job.setJobName(templateType + "导出 - " + LocalDateTime.now().toString());
        job.setJobType(JobType.EXPORT.name());
        job.setState(JobState.PENDING.name());
        job.setFilePath(filters); // 过滤条件暂存在 filePath 字段中
        job.setTotalItems(0);
        job.setProcessedItems(0);
        job.setSuccessItems(0);
        job.setErrorItems(0);
        baseMapper.insert(job);
        log.info("[BatchService] 导出任务已创建: id={}, jobName={}", job.getId(), job.getJobName());
        return job;
    }

    /**
     * 更新批处理任务的进度。
     * <p>
     * 更新指定任务的已处理条目数、成功条目数和失败条目数。
     * 通常由异步执行模块在处理过程中周期性回调此方法，以反映实时进度。
     * </p>
     *
     * @param id             任务 ID
     * @param processedItems 已处理条目数
     * @param successItems   成功条目数
     * @param errorItems     失败条目数
     */
    @Transactional
    public void updateProgress(Long id, int processedItems, int successItems, int errorItems) {
        log.info("[BatchService] 更新任务进度: id={}, processed={}, success={}, error={}",
                id, processedItems, successItems, errorItems);
        BatchJob job = new BatchJob();
        job.setId(id);
        job.setProcessedItems(processedItems);
        job.setSuccessItems(successItems);
        job.setErrorItems(errorItems);
        baseMapper.updateById(job);
    }

    /**
     * 标记批处理任务为已完成。
     * <p>
     * 将任务状态更新为 COMPLETED，并记录完成时间。
     * 仅在任务所有条目处理完毕且无致命错误时调用。
     * </p>
     *
     * @param id 任务 ID
     */
    @Transactional
    public void completeJob(Long id) {
        log.info("[BatchService] 标记任务完成: id={}", id);
        BatchJob job = new BatchJob();
        job.setId(id);
        job.setState(JobState.COMPLETED.name());
        job.setCompletedAt(LocalDateTime.now());
        baseMapper.updateById(job);
    }

    /**
     * 标记批处理任务为失败。
     * <p>
     * 将任务状态更新为 FAILED，并记录完成时间。
     * 当任务执行过程中发生不可恢复的错误时调用此方法。
     * 错误信息可记录在日志中，供后续排查使用。
     * </p>
     *
     * @param id           任务 ID
     * @param errorMessage 错误信息描述
     */
    @Transactional
    public void failJob(Long id, String errorMessage) {
        log.error("[BatchService] 标记任务失败: id={}, error={}", id, errorMessage);
        BatchJob job = new BatchJob();
        job.setId(id);
        job.setState(JobState.FAILED.name());
        job.setErrorMessage(errorMessage);
        job.setCompletedAt(LocalDateTime.now());
        baseMapper.updateById(job);
    }

    /**
     * 标记任务为运行中。
     */
    @Transactional
    public void markRunning(Long id) {
        log.info("[BatchService] 标记任务运行中: id={}", id);
        BatchJob job = new BatchJob();
        job.setId(id);
        job.setState(JobState.RUNNING.name());
        baseMapper.updateById(job);
    }

    /**
     * 创建并异步执行批处理任务。
     * 创建任务后立即提交到异步线程池执行。
     */
    @Transactional
    public BatchJob startJob(String jobType, String templateType, String filePath, String filters, int totalItems) {
        BatchJob job;
        if (JobType.IMPORT.name().equalsIgnoreCase(jobType)) {
            job = startImport(filePath, templateType);
        } else {
            job = startExport(templateType, filters);
        }
        job.setTotalItems(totalItems);
        baseMapper.updateById(job);

        // 异步提交执行
        batchJobExecutor.execute(job);
        return job;
    }

    /**
     * 重试任务中所有失败的条目。
     * 将所有 FAILED 条目标记为 FAILED_RETRY，然后异步重试。
     *
     * @param jobId 任务ID
     */
    @Transactional
    public void retryFailedItems(Long jobId) {
        List<BatchJobItem> failedItems = itemService.listFailedItems(jobId);
        if (failedItems.isEmpty()) {
            log.info("[BatchService] 无失败条目可重试: jobId={}", jobId);
            return;
        }
        // 标记为待重试
        for (BatchJobItem item : failedItems) {
            itemService.markRetry(item.getId());
        }
        log.info("[BatchService] 已标记 {} 条待重试: jobId={}", failedItems.size(), jobId);

        // 异步执行重试
        BatchJob job = getJob(jobId);
        if (job != null) {
            batchJobExecutor.retry(job);
        }
    }

    /**
     * 生成错误明细导出内容。
     * 将失败条目的关键信息拼接为 CSV 文本，供前端下载或文件导出。
     *
     * @param jobId 任务ID
     * @return CSV 格式的错误明细文本，无失败条目时返回空字符串
     */
    public String generateErrorFile(Long jobId) {
        List<BatchJobItem> failedItems = itemService.listFailedItems(jobId);
        if (failedItems.isEmpty()) {
            return "";
        }
        // 生成 CSV 表头 + 数据行
        StringBuilder sb = new StringBuilder();
        sb.append("行号,条目标识,失败原因,处理时间\n");
        for (BatchJobItem item : failedItems) {
            sb.append(item.getRowIndex()).append(",");
            sb.append(escapeCsv(item.getItemKey())).append(",");
            sb.append(escapeCsv(item.getErrorMessage())).append(",");
            sb.append(item.getProcessedAt() != null ? item.getProcessedAt().toString() : "").append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
