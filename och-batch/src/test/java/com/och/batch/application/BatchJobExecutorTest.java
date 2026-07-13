package com.och.batch.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.batch.domain.BatchJob;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.domain.ItemState;
import com.och.batch.service.IBatchJobItemService;
import com.och.batch.service.IBatchService;
import com.och.batch.service.impl.BatchJobExecutorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchJobExecutor 单元测试")
class BatchJobExecutorTest {

    @Mock private IBatchService batchService;
    @Mock private IBatchJobItemService itemService;
    @InjectMocks private BatchJobExecutorImpl executor;

    @Test @DisplayName("execute: IMPORT正常完成")
    void executeImport_shouldCompleteJob() {
        BatchJob job = new BatchJob(); job.setId(1L); job.setJobType("IMPORT"); job.setJobName("测试导入");
        IPage<BatchJobItem> emptyPage = new Page<>(1, 20);
        emptyPage.setRecords(Collections.emptyList()); emptyPage.setTotal(0L);
        when(itemService.listItems(eq(1L), anyInt(), anyInt())).thenReturn(emptyPage);
        executor.execute(job);
        verify(batchService).markRunning(1L);
        verify(batchService).completeJob(1L);
    }

    @Test @DisplayName("execute: IMPORT异常标记失败")
    void executeImport_shouldFailJobOnException() {
        BatchJob job = new BatchJob(); job.setId(1L); job.setJobType("IMPORT"); job.setJobName("测试导入");
        when(itemService.listItems(anyLong(), anyInt(), anyInt())).thenThrow(new RuntimeException("文件解析失败"));
        executor.execute(job);
        verify(batchService).markRunning(1L);
        verify(batchService).failJob(eq(1L), contains("执行异常"));
    }

    @Test @DisplayName("execute: EXPORT完成")
    void executeExport_shouldCompleteJob() {
        BatchJob job = new BatchJob(); job.setId(2L); job.setJobType("EXPORT"); job.setJobName("测试导出");
        executor.execute(job);
        verify(batchService).markRunning(2L);
        verify(batchService).completeJob(2L);
    }

    @Test @DisplayName("retry: 无待重试条目时直接返回")
    void retry_shouldReturnWhenNoRetryItems() {
        BatchJob job = new BatchJob(); job.setId(1L);
        when(itemService.listRetryItems(1L)).thenReturn(Collections.emptyList());
        executor.retry(job);
        verify(itemService).listRetryItems(1L);
        verify(itemService, never()).markSuccess(anyLong());
    }

    @Test @DisplayName("retry: 重试成功更新状态")
    void retry_shouldMarkSuccessWhenRetrySucceeds() {
        BatchJob job = new BatchJob(); job.setId(1L); job.setProcessedItems(10); job.setSuccessItems(8); job.setErrorItems(2);
        BatchJobItem item = new BatchJobItem(); item.setId(100L); item.setRowIndex(3);
        when(itemService.listRetryItems(1L)).thenReturn(List.of(item));
        when(itemService.countByState(eq(1L), eq(ItemState.FAILED))).thenReturn(0L);
        executor.retry(job);
        verify(itemService).markSuccess(100L);
        verify(batchService).updateProgress(eq(1L), anyInt(), anyInt(), anyInt());
    }

    @Test @DisplayName("execute: 未知类型标记失败")
    void execute_shouldFailJobForUnknownType() {
        BatchJob job = new BatchJob(); job.setId(1L); job.setJobType("UNKNOWN"); job.setJobName("未知类型");
        executor.execute(job);
        verify(batchService).markRunning(1L);
        verify(batchService).failJob(eq(1L), contains("未知任务类型"));
    }
}
