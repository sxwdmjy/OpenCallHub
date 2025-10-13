package com.och.common.config;

import com.och.common.thread.ThreadFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 * @author danmo
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Value("${thread-pool.core-pool-size:10}")
    private int corePoolSize;

    @Value("${thread-pool.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${thread-pool.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${thread-pool.thread-name-prefix:och-}")
    private String threadNamePrefix;

    /**
     * 通用异步任务线程池
     */
    @Bean("asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix + "async-");
        executor.setThreadFactory(new ThreadFactoryImpl(threadNamePrefix + "async-", false));
        
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("异步任务线程池初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
            corePoolSize, maxPoolSize, queueCapacity);
        
        return executor;
    }

    /**
     * 文件处理线程池
     */
    @Bean("fileProcessExecutor")
    public ThreadPoolTaskExecutor fileProcessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(threadNamePrefix + "file-");
        executor.setThreadFactory(new ThreadFactoryImpl(threadNamePrefix + "file-", false));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("文件处理线程池初始化完成");
        return executor;
    }

    /**
     * 数据库操作线程池
     */
    @Bean("dbOperationExecutor")
    public ThreadPoolTaskExecutor dbOperationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(threadNamePrefix + "db-");
        executor.setThreadFactory(new ThreadFactoryImpl(threadNamePrefix + "db-", false));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("数据库操作线程池初始化完成");
        return executor;
    }

    /**
     * 网络IO线程池
     */
    @Bean("networkIoExecutor")
    public ThreadPoolTaskExecutor networkIoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(25);
        executor.setQueueCapacity(800);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(threadNamePrefix + "net-");
        executor.setThreadFactory(new ThreadFactoryImpl(threadNamePrefix + "net-", false));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("网络IO线程池初始化完成");
        return executor;
    }
}
