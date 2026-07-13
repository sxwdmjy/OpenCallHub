package com.och.batch.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.batch.domain.BatchJob;

import java.util.List;

public interface IBatchService {
    IPage<BatchJob> listJobs(int pageNum, int pageSize);
    BatchJob getJob(Long id);
    BatchJob startImport(String fileUrl, String templateType);
    BatchJob startExport(String templateType, String filters);
    void updateProgress(Long id, int processedItems, int successItems, int errorItems);
    void completeJob(Long id);
    void failJob(Long id, String errorMessage);
    void markRunning(Long id);
    BatchJob startJob(String jobType, String templateType, String filePath, String filters, int totalItems);
    void retryFailedItems(Long jobId);
    String generateErrorFile(Long jobId);
}
