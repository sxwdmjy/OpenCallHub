package com.och.esl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * ESL 事件异步发布线程池配置
 *
 * <p>为 {@link com.och.esl.application.EslEventAsyncPublisher#asyncPublish}
 * 提供专用的线程池，核心4线程/最大16线程/队列1000，避免 FreeSWITCH 高频事件推送
 * （500+ 事件/秒）阻塞 ESL 回调线程。</p>
 *
 * <p>拒绝策略使用 CallerRunsPolicy：队列满时由调用线程（即 <em>esl-event-*</em> 线程）
 * 同步执行，防止事件丢失，同时形成自然背压。</p>
 *
 * @author danmo
 * @see com.och.esl.application.EslEventAsyncPublisher
 */
@Configuration
@EnableAsync
public class EslEventAsyncConfig {

    /**
     * ESL 事件专用异步线程池
     *
     * <p>该线程池负责接收 {@link com.och.esl.application.EslEventAsyncPublisher#asyncPublish}
     * 投递的异步任务，将发布动作从 FreeSWITCH 回调线程中剥离。</p>
     *
     * <ul>
     *   <li>corePoolSize=4  — 常驻4个线程处理常规事件负载</li>
     *   <li>maxPoolSize=16  — 高峰期扩容至16线程</li>
     *   <li>queueCapacity=1000 — 缓冲队列，峰值削峰</li>
     *   <li>CallerRunsPolicy — 队列满时触发背压，由调用方线程执行</li>
     * </ul>
     */
    @Bean("eslEventTaskExecutor")
    public ThreadPoolTaskExecutor eslEventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("esl-event-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
