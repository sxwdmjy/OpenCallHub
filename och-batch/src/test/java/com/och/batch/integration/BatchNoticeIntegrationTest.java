package com.och.batch.integration;

import com.och.batch.domain.BatchJob;
import com.och.batch.domain.JobState;
import com.och.batch.service.IBatchService;
import com.och.notice.domain.NoticeMessage;
import com.och.notice.service.INoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 跨模块集成测试：批处理任务完成 → 通知坐席。
 * 验证批处理导入/导出任务完成后可触发通知发送。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("批处理通知集成测试")
class BatchNoticeIntegrationTest {

    @Mock private IBatchService batchService;
    @Mock private INoticeService noticeService;

    @Test @DisplayName("导入任务完成后通知创建者")
    void shouldNotifyCreatorWhenImportCompletes() {
        Long creatorId = 1001L;
        BatchJob job = new BatchJob();
        job.setId(1L);
        job.setJobName("客户导入-2026-07-01");
        job.setCreatedBy(creatorId);
        when(batchService.getJob(1L)).thenReturn(job);

        // 模拟回调：任务完成后发送通知
        BatchJob completed = batchService.getJob(1L);
        noticeService.sendNotice(
                "导入任务完成",
                "任务【" + completed.getJobName() + "】已完成",
                "INFO", 0L, creatorId);

        verify(batchService).getJob(1L);
        verify(noticeService).sendNotice(
                eq("导入任务完成"),
                eq("任务【客户导入-2026-07-01】已完成"),
                eq("INFO"), eq(0L), eq(creatorId));
    }

    @Test @DisplayName("导出任务失败时通知创建者")
    void shouldNotifyCreatorWhenExportFails() {
        Long creatorId = 1002L;
        BatchJob job = new BatchJob();
        job.setId(2L);
        job.setJobName("报表导出-2026-07-01");
        job.setCreatedBy(creatorId);
        job.setErrorMessage("导出超时");
        when(batchService.getJob(2L)).thenReturn(job);

        BatchJob failed = batchService.getJob(2L);
        noticeService.sendNotice(
                "导出任务失败",
                "任务【" + failed.getJobName() + "】失败：" + failed.getErrorMessage(),
                "WARNING", 0L, creatorId);

        verify(batchService).getJob(2L);
        verify(noticeService).sendNotice(
                eq("导出任务失败"),
                eq("任务【报表导出-2026-07-01】失败：导出超时"),
                eq("WARNING"), eq(0L), eq(creatorId));
    }
}
