package com.och.batch.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.batch.domain.BatchJob;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.mapper.BatchJobMapper;
import com.och.batch.service.IBatchJobExecutor;
import com.och.batch.service.IBatchJobItemService;
import com.och.batch.service.impl.BatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock private BatchJobMapper batchJobMapper;
    @Mock private IBatchJobItemService itemService;
    @Mock private IBatchJobExecutor batchJobExecutor;
    @InjectMocks private BatchServiceImpl batchService;

    @BeforeEach
    void setUp() { ReflectionTestUtils.setField(batchService, "baseMapper", batchJobMapper); }

    @Test void startImport_shouldCreatePendingImportJob() {
        String fileUrl = "https://example.com/files/import.xlsx";
        String templateType = "CUSTOMER";
        BatchJob result = batchService.startImport(fileUrl, templateType);
        assertNotNull(result);
        assertEquals("IMPORT", result.getJobType());
        assertEquals("PENDING", result.getState());
        assertEquals(fileUrl, result.getFilePath());
        verify(batchJobMapper).insert(result);
    }

    @Test void startExport_shouldCreatePendingExportJob() {
        String templateType = "ORDER";
        String filters = "{\"startDate\":\"2025-01-01\"}";
        BatchJob result = batchService.startExport(templateType, filters);
        assertEquals("EXPORT", result.getJobType());
        assertEquals(filters, result.getFilePath());
        verify(batchJobMapper).insert(result);
    }

    @Test void getJob_shouldReturnJob_whenJobExists() {
        Long jobId = 1L;
        BatchJob expected = buildJob(jobId, "IMPORT", "PENDING");
        when(batchJobMapper.selectById(jobId)).thenReturn(expected);
        BatchJob result = batchService.getJob(jobId);
        assertNotNull(result);
        assertEquals(jobId, result.getId());
        verify(batchJobMapper).selectById(jobId);
    }

    @Test void getJob_shouldReturnNull_whenJobNotFound() {
        Long jobId = 999L;
        when(batchJobMapper.selectById(jobId)).thenReturn(null);
        assertNull(batchService.getJob(jobId));
    }

    @Test @SuppressWarnings("unchecked")
    void listJobs_shouldReturnPaginatedResults() {
        BatchJob job1 = buildJob(1L, "IMPORT", "PENDING");
        BatchJob job2 = buildJob(2L, "EXPORT", "COMPLETED");
        Page<BatchJob> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(job1, job2)); page.setTotal(2L);
        when(batchJobMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        IPage<BatchJob> result = batchService.listJobs(1, 20);
        assertEquals(2, result.getRecords().size());
        assertEquals(2L, result.getTotal());
    }

    @Test void updateProgress_shouldUpdateCounters() {
        Long jobId = 1L;
        batchService.updateProgress(jobId, 80, 75, 5);
        ArgumentCaptor<BatchJob> captor = ArgumentCaptor.forClass(BatchJob.class);
        verify(batchJobMapper).updateById(captor.capture());
        assertEquals(jobId, captor.getValue().getId());
        assertEquals(80, captor.getValue().getProcessedItems());
    }

    @Test void completeJob_shouldSetStateToCompleted() {
        Long jobId = 1L;
        batchService.completeJob(jobId);
        ArgumentCaptor<BatchJob> captor = ArgumentCaptor.forClass(BatchJob.class);
        verify(batchJobMapper).updateById(captor.capture());
        assertEquals("COMPLETED", captor.getValue().getState());
        assertNotNull(captor.getValue().getCompletedAt());
    }

    @Test void failJob_shouldSetStateToFailed() {
        Long jobId = 1L;
        String errorMessage = "文件格式校验失败";
        batchService.failJob(jobId, errorMessage);
        ArgumentCaptor<BatchJob> captor = ArgumentCaptor.forClass(BatchJob.class);
        verify(batchJobMapper).updateById(captor.capture());
        BatchJob failed = captor.getValue();
        assertEquals("FAILED", failed.getState());
        assertEquals(errorMessage, failed.getErrorMessage());
    }

    @Test void stateTransition_shouldFlowFromPendingToCompleted() {
        Long jobId = 1L;
        BatchJob job = buildJob(jobId, "IMPORT", "PENDING");
        when(batchJobMapper.selectById(jobId)).thenReturn(job);
        assertEquals("PENDING", batchService.getJob(jobId).getState());
        batchService.completeJob(jobId);
        ArgumentCaptor<BatchJob> captor = ArgumentCaptor.forClass(BatchJob.class);
        verify(batchJobMapper).updateById(captor.capture());
        assertEquals("COMPLETED", captor.getValue().getState());
    }

    @Test void markRunning_shouldSetStateToRunning() {
        batchService.markRunning(1L);
        ArgumentCaptor<BatchJob> captor = ArgumentCaptor.forClass(BatchJob.class);
        verify(batchJobMapper).updateById(captor.capture());
        assertEquals("RUNNING", captor.getValue().getState());
    }

    @Test void retryFailedItems_shouldMarkAsRetryAndExecute() {
        Long jobId = 1L;
        BatchJobItem item = new BatchJobItem(); item.setId(100L); item.setJobId(jobId);
        when(itemService.listFailedItems(jobId)).thenReturn(List.of(item));
        BatchJob job = buildJob(jobId, "IMPORT", "COMPLETED");
        when(batchJobMapper.selectById(jobId)).thenReturn(job);
        batchService.retryFailedItems(jobId);
        verify(itemService).markRetry(100L);
        verify(batchJobExecutor).retry(job);
    }

    @Test void retryFailedItems_shouldSkipWhenNoFailedItems() {
        when(itemService.listFailedItems(1L)).thenReturn(Collections.emptyList());
        batchService.retryFailedItems(1L);
        verify(itemService, never()).markRetry(anyLong());
    }

    @Test void generateErrorFile_shouldReturnCsvWithFailedItems() {
        BatchJobItem item = new BatchJobItem();
        item.setRowIndex(3); item.setItemKey("KEY001"); item.setErrorMessage("校验失败");
        item.setProcessedAt(LocalDateTime.now());
        when(itemService.listFailedItems(1L)).thenReturn(List.of(item));
        String result = batchService.generateErrorFile(1L);
        assertTrue(result.contains("行号,条目标识,失败原因"));
        assertTrue(result.contains("KEY001"));
    }

    @Test void generateErrorFile_shouldReturnEmptyWhenNoFailedItems() {
        when(itemService.listFailedItems(1L)).thenReturn(Collections.emptyList());
        assertEquals("", batchService.generateErrorFile(1L));
    }

    private BatchJob buildJob(Long id, String jobType, String state) {
        BatchJob job = new BatchJob();
        job.setId(id); job.setJobName("测试任务-" + id);
        job.setJobType(jobType); job.setState(state);
        job.setTotalItems(100); job.setProcessedItems(0);
        job.setSuccessItems(0); job.setErrorItems(0);
        job.setFilePath("/data/test.xlsx");
        job.setCreatedAt(LocalDateTime.now());
        return job;
    }
}
