package com.och.calltask.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 外呼任务定时调度配置
 * <p>
 * 替代 Quartz 的轻量级定时任务方案，使用 Spring 内置的 {@code @Scheduled} 注解驱动。
 * 线程池配置为 2 核心 / 4 最大，足以支撑预测外呼的高频轮询（2s间隔）与低频清理任务。
 * </p>
 *
 * @author danmo
 */
@Configuration
@EnableScheduling
public class OutboundTaskSchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("och-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }
}
