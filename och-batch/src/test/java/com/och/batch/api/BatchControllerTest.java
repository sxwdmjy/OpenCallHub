package com.och.batch.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.batch.service.IBatchJobItemService;
import com.och.batch.service.IBatchService;
import com.och.batch.domain.BatchJob;
import com.och.batch.domain.BatchJobItem;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.common.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BatchController 单元测试
 * <p>验证批处理管理控制器的 REST API 端点处理逻辑。</p>
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

    @Mock
    private IBatchService batchService;

    @Mock
    private IBatchJobItemService itemService;

    @InjectMocks
    private BatchController controller;

    @BeforeEach
    void setUp() {
        TraceContext.setTraceId("batch-test-trace-001");
    }

    @AfterEach
    void tearDown() {
        TraceContext.remove();
    }

    // ---------- listJobs ----------

    @Test
    @SuppressWarnings("unchecked")
    void listJobs_shouldReturnPageResponse_whenJobsExist() {
        // given
        int pageNum = 1;
        int pageSize = 20;
        BatchJob job1 = buildJob(1L, "IMPORT", "PENDING");
        BatchJob job2 = buildJob(2L, "EXPORT", "COMPLETED");
        List<BatchJob> records = Arrays.asList(job1, job2);
        Page<BatchJob> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(2L);
        when(batchService.listJobs(pageNum, pageSize)).thenReturn(page);

        // when
        ApiResponse<PageResponse<BatchJob>> response = controller.listJobs(pageNum, pageSize);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("batch-test-trace-001", response.getTraceId());

        PageResponse<BatchJob> data = response.getData();
        assertNotNull(data);
        assertEquals(2, data.getRecords().size());
        assertEquals(2L, data.getTotal());
        assertEquals(pageNum, data.getPageNum());
        assertEquals(pageSize, data.getPageSize());

        verify(batchService).listJobs(pageNum, pageSize);
    }

    @Test
    void listJobs_shouldUseDefaultPagination_whenNoParamsProvided() {
        // given
        int defaultPageNum = 1;
        int defaultPageSize = 20;
        Page<BatchJob> emptyPage = new Page<>(defaultPageNum, defaultPageSize);
        emptyPage.setRecords(Arrays.asList());
        emptyPage.setTotal(0L);
        when(batchService.listJobs(defaultPageNum, defaultPageSize)).thenReturn(emptyPage);

        // when
        ApiResponse<PageResponse<BatchJob>> response = controller.listJobs(defaultPageNum, defaultPageSize);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());

        PageResponse<BatchJob> data = response.getData();
        assertNotNull(data);
        assertTrue(data.getRecords().isEmpty());
        assertEquals(0L, data.getTotal());

        verify(batchService).listJobs(defaultPageNum, defaultPageSize);
    }

    // ---------- getJob ----------

    @Test
    void getJob_shouldReturnJobDetail_whenJobExists() {
        // given
        Long jobId = 1L;
        BatchJob job = buildJob(jobId, "IMPORT", "RUNNING");
        when(batchService.getJob(jobId)).thenReturn(job);

        // when
        ApiResponse<BatchJob> response = controller.getJob(jobId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("batch-test-trace-001", response.getTraceId());

        BatchJob data = response.getData();
        assertNotNull(data);
        assertEquals(jobId, data.getId());
        assertEquals("RUNNING", data.getState());

        verify(batchService).getJob(jobId);
    }

    // ---------- startImport ----------

    @Test
    void startImport_shouldReturnCreatedImportJob() {
        // given
        String fileUrl = "https://example.com/files/import.xlsx";
        String templateType = "CUSTOMER";
        BatchJob createdJob = buildJob(1L, "IMPORT", "PENDING");
        when(batchService.startImport(fileUrl, templateType)).thenReturn(createdJob);

        // when
        ApiResponse<BatchJob> response = controller.startImport(fileUrl, templateType);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("IMPORT", response.getData().getJobType());
        assertEquals("PENDING", response.getData().getState());

        verify(batchService).startImport(fileUrl, templateType);
    }

    // ---------- startExport ----------

    @Test
    void startExport_shouldReturnCreatedExportJob() {
        // given
        String templateType = "ORDER";
        String filters = "{\"status\":\"active\"}";
        BatchJob createdJob = buildJob(2L, "EXPORT", "PENDING");
        when(batchService.startExport(templateType, filters)).thenReturn(createdJob);

        // when
        ApiResponse<BatchJob> response = controller.startExport(templateType, filters);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("EXPORT", response.getData().getJobType());
        assertEquals("PENDING", response.getData().getState());

        verify(batchService).startExport(templateType, filters);
    }

    @Test
    void startExport_shouldUseDefaultFilters_whenFiltersNotProvided() {
        // given
        String templateType = "ORDER";
        String defaultFilters = "{}";
        BatchJob createdJob = buildJob(2L, "EXPORT", "PENDING");
        when(batchService.startExport(templateType, defaultFilters)).thenReturn(createdJob);

        // when
        ApiResponse<BatchJob> response = controller.startExport(templateType, defaultFilters);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("EXPORT", response.getData().getJobType());

        verify(batchService).startExport(templateType, defaultFilters);
    }

    // ---------- updateProgress ----------

    @Test
    void updateProgress_shouldReturnSuccess() {
        // given
        Long jobId = 1L;
        int processed = 80;
        int success = 75;
        int error = 5;
        doNothing().when(batchService).updateProgress(jobId, processed, success, error);

        // when
        ApiResponse<Void> response = controller.updateProgress(jobId, processed, success, error);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNull(response.getData());

        verify(batchService).updateProgress(jobId, processed, success, error);
    }

    // ---------- completeJob ----------

    @Test
    void completeJob_shouldReturnSuccess() {
        // given
        Long jobId = 1L;
        doNothing().when(batchService).completeJob(jobId);

        // when
        ApiResponse<Void> response = controller.completeJob(jobId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNull(response.getData());

        verify(batchService).completeJob(jobId);
    }

    // ---------- failJob ----------

    @Test
    void failJob_shouldReturnSuccess() {
        // given
        Long jobId = 1L;
        String errorMessage = "导出过程发生数据库连接超时";
        doNothing().when(batchService).failJob(jobId, errorMessage);

        // when
        ApiResponse<Void> response = controller.failJob(jobId, errorMessage);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNull(response.getData());

        verify(batchService).failJob(jobId, errorMessage);
    }

    // ---------- listItems ----------

    @Test
    void listItems_shouldReturnPaginatedItems() {
        Long jobId = 1L;
        BatchJobItem item = new BatchJobItem();
        item.setId(1L);
        item.setJobId(jobId);
        Page<BatchJobItem> page = new Page<>(1, 20);
        page.setRecords(List.of(item));
        page.setTotal(1L);
        when(itemService.listItems(jobId, 1, 20)).thenReturn(page);

        ApiResponse<PageResponse<BatchJobItem>> response = controller.listItems(jobId, 1, 20);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1L, response.getData().getTotal());
        verify(itemService).listItems(jobId, 1, 20);
    }

    // ---------- retryFailedItems ----------

    @Test
    void retryFailedItems_shouldReturnSuccess() {
        Long jobId = 1L;
        doNothing().when(batchService).retryFailedItems(jobId);

        ApiResponse<Void> response = controller.retryFailedItems(jobId);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(batchService).retryFailedItems(jobId);
    }

    // ---------- exportErrors ----------

    @Test
    void exportErrors_shouldReturnCsvContent() {
        Long jobId = 1L;
        String csv = "行号,条目标识,失败原因\n1,KEY001,校验失败\n";
        when(batchService.generateErrorFile(jobId)).thenReturn(csv);

        ApiResponse<String> response = controller.exportErrors(jobId);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(csv, response.getData());
        verify(batchService).generateErrorFile(jobId);
    }

    @Test
    void exportErrors_shouldReturnEmptyWhenNoErrors() {
        Long jobId = 1L;
        when(batchService.generateErrorFile(jobId)).thenReturn("");

        ApiResponse<String> response = controller.exportErrors(jobId);

        assertEquals(200, response.getCode());
        assertEquals("", response.getData());
        verify(batchService).generateErrorFile(jobId);
    }

    // ---------- helpers ----------

    private BatchJob buildJob(Long id, String jobType, String state) {
        BatchJob job = new BatchJob();
        job.setId(id);
        job.setJobName("测试任务-" + id);
        job.setJobType(jobType);
        job.setState(state);
        job.setTotalItems(100);
        job.setProcessedItems(0);
        job.setSuccessItems(0);
        job.setErrorItems(0);
        job.setFilePath("/data/test.xlsx");
        job.setCreatedAt(LocalDateTime.now());
        return job;
    }
}
