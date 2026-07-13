package com.och.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 批处理异步执行线程池配置。
 * <p>
 * 为 {@link com.och.batch.service.IBatchJobExecutor} 提供专用线程池，
 * 核心2线程/最大4线程/队列100，避免批量任务阻塞主线程。
 * 拒绝策略使用 CallerRunsPolicy：队列满时由调用线程同步执行，形成自然背压。
 * </p>
 */
@Configuration
public class BatchAsyncConfig {

    @Bean("batchTaskExecutor")
    public ThreadPoolTaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("och-batch-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
