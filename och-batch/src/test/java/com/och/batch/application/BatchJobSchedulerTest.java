package com.och.batch.application;

import com.och.batch.domain.BatchJob;
import com.och.batch.domain.JobState;
import com.och.batch.mapper.BatchJobMapper;
import com.och.batch.service.impl.BatchJobExecutorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("批处理任务调度器测试")
class BatchJobSchedulerTest {

    @Mock private BatchJobMapper batchJobMapper;
    @Mock private BatchJobExecutorImpl executor;
    @InjectMocks private BatchJobScheduler scheduler;

    @Test @DisplayName("无待处理任务时不触发执行")
    void shouldNotExecuteWhenNoPendingJobs() {
        when(batchJobMapper.selectList(any())).thenReturn(Collections.emptyList());
        scheduler.pollPendingJobs();
        verify(executor, never()).execute(any());
    }

    @Test @DisplayName("发现待处理任务时提交执行")
    void shouldExecutePendingJobs() {
        BatchJob job1 = new BatchJob(); job1.setId(1L); job1.setState(JobState.PENDING.name());
        BatchJob job2 = new BatchJob(); job2.setId(2L); job2.setState(JobState.PENDING.name());
        when(batchJobMapper.selectList(any())).thenReturn(List.of(job1, job2));

        scheduler.pollPendingJobs();

        verify(executor).execute(job1);
        verify(executor).execute(job2);
    }

    @Test @DisplayName("提交异常不阻塞其他任务")
    void shouldContinueOnExecutionError() {
        BatchJob job = new BatchJob(); job.setId(1L); job.setState(JobState.PENDING.name());
        when(batchJobMapper.selectList(any())).thenReturn(List.of(job));
        doThrow(new RuntimeException("执行异常")).when(executor).execute(job);

        // 不应抛出异常
        scheduler.pollPendingJobs();
        verify(executor).execute(job);
    }
}
