package com.och.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author danmo
 */
@Slf4j
public class ThreadPoolUtil {

    private static final int BLOCKING_QUEUE_LENGTH = 100000;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final String APPLICATION_EVENT_POOL_NAME = "application-event-pool";
    private static volatile ThreadPoolTaskExecutor applicationEventThreadPool = null;

    public static ThreadPoolTaskExecutor getApplicationEventThreadPool() {
        if (applicationEventThreadPool == null) {
            synchronized (ThreadPoolUtil.class) {
                if (applicationEventThreadPool == null) {
                    // 获取处理器数量
                    int cpuNum = Runtime.getRuntime().availableProcessors();
                    // 根据cpu数量,计算出合理的线程并发数
                    int maximumPoolSize = cpuNum * 2 + 1;

                    applicationEventThreadPool = new ThreadPoolTaskExecutor();
                    applicationEventThreadPool.setCorePoolSize(maximumPoolSize - 1);
                    applicationEventThreadPool.setMaxPoolSize(maximumPoolSize);
                    applicationEventThreadPool.setKeepAliveSeconds(KEEP_ALIVE_TIME);
                    applicationEventThreadPool.setQueueCapacity(BLOCKING_QUEUE_LENGTH);
                    applicationEventThreadPool.setThreadNamePrefix(APPLICATION_EVENT_POOL_NAME);
                    applicationEventThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                            log.warn("触发器线程爆炸了，当前运行线程总数：{}，活动线程数：{}。等待队列已满，等待运行任务数：{}",
                                    e.getPoolSize(),
                                    e.getActiveCount(),
                                    e.getQueue().size());
                        }
                    });
                    applicationEventThreadPool.initialize();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationEventThreadPool.setWaitForTasksToCompleteOnShutdown(true)));
                }
            }
        }
        return applicationEventThreadPool;
    }
}
