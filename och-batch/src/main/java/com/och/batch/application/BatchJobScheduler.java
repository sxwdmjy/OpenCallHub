package com.och.batch.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.batch.domain.BatchJob;
import com.och.batch.domain.JobState;
import com.och.batch.mapper.BatchJobMapper;
import com.och.batch.service.impl.BatchJobExecutorImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 批处理任务调度器。
 * <p>
 * 每隔 10 秒扫描 PENDING 状态的批处理任务，提交给 {@link BatchJobExecutor} 异步执行。
 * 仅在 och.batch.scheduler.enabled=true 时启用（默认开启）。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "och.batch.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BatchJobScheduler {

    private final BatchJobMapper batchJobMapper;
    private final BatchJobExecutorImpl executor;

    /**
     * 定时扫描待处理的批处理任务并提交执行。
     * 每次最多取 10 条 PENDING 任务，避免单次加载过多。
     */
    @Scheduled(fixedDelay = 10000)
    public void pollPendingJobs() {
        LambdaQueryWrapper<BatchJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchJob::getState, JobState.PENDING.name());
        wrapper.orderByAsc(BatchJob::getCreatedAt);
        wrapper.last("LIMIT 10");

        List<BatchJob> pendingJobs = batchJobMapper.selectList(wrapper);
        if (pendingJobs.isEmpty()) {
            return;
        }

        log.info("[BatchScheduler] 发现 {} 个待处理任务", pendingJobs.size());
        for (BatchJob job : pendingJobs) {
            try {
                executor.execute(job);
            } catch (Exception e) {
                log.error("[BatchScheduler] 提交任务失败: id={}", job.getId(), e);
            }
        }
    }
}
