package com.och.esl.service.impl;

import com.och.common.thread.ThreadFactoryImpl;
import com.och.common.utils.ThreadUtils;
import com.och.esl.FsEslEventRunnable;
import com.och.esl.FsEslMsg;
import com.och.esl.factory.FsEslEventFactory;
import com.och.esl.service.IFsEslEventService;
import com.och.esl.utils.EslEventUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author danmo
 * @date 2023-10-20 10:46
 **/
@Slf4j
@Service
public class IFsEslEventServiceImpl implements IFsEslEventService, InitializingBean {

    @Value("${freeswitch.thread-num:64}")
    private Integer threadNum;

    @Value("${freeswitch.queue-capacity:10000}")
    private Integer queueCapacity;

    @Autowired
    private FsEslEventFactory factory;

    private final Map<Integer, ThreadPoolExecutor> executorMap = new ConcurrentHashMap<>();
    private final AtomicInteger rejectedCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);

    @Override
    public void eslEventPublisher(FsEslMsg msg) {
        ExecutorService executorService = getExecutorService(msg.getEslEvent());
        try {
            executorService.execute(new FsEslEventRunnable(factory, msg));
            processedCount.incrementAndGet();
        } catch (RejectedExecutionException e) {
            rejectedCount.incrementAndGet();
            log.warn("ESL事件被拒绝执行，事件类型: {}, 唯一ID: {}", 
                msg.getEslEvent().getEventName(), 
                EslEventUtil.getUniqueId(msg.getEslEvent()));
            // 可以考虑降级处理或重试机制
        }
    }

    @Override
    public void destroyThreadPool() {
        log.info("开始销毁ESL事件线程池，共{}个线程池", executorMap.size());
        executorMap.values().forEach(pool -> {
            if (pool != null && !pool.isShutdown()) {
                ThreadUtils.shutdownAndAwaitTermination(pool);
            }
        });
        executorMap.clear();
        log.info("ESL事件线程池销毁完成，处理事件总数: {}, 拒绝事件总数: {}", 
            processedCount.get(), rejectedCount.get());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("初始化ESL事件线程池，线程数: {}, 队列容量: {}", threadNum, queueCapacity);
        
        for (int i = 0; i < threadNum; i++) {
            ThreadPoolExecutor singleExecutor = new ThreadPoolExecutor(
                1, 1,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadFactoryImpl("esl-event-pool-" + i + "-", false),
                new ThreadPoolExecutor.CallerRunsPolicy() // 使用CallerRunsPolicy避免任务丢失
            );
            
            // 添加监控
            singleExecutor.setThreadFactory(new ThreadFactoryImpl("esl-event-pool-" + i + "-", false) {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = super.newThread(r);
                    t.setUncaughtExceptionHandler((thread, throwable) -> 
                        log.error("ESL事件处理线程异常，线程名: {}", thread.getName(), throwable));
                    return t;
                }
            });
            
            executorMap.put(i, singleExecutor);
        }
        
        // 启动监控线程
        startMonitoringThread();
    }

    private void startMonitoringThread() {
        Thread monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000); // 每30秒监控一次
                    logThreadPoolStatus();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "esl-monitor-thread");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void logThreadPoolStatus() {
        executorMap.forEach((index, pool) -> {
            if (pool != null) {
                log.debug("线程池[{}]状态 - 活跃线程: {}, 队列大小: {}, 已完成任务: {}", 
                    index, pool.getActiveCount(), pool.getQueue().size(), pool.getCompletedTaskCount());
            }
        });
    }

    private ExecutorService getExecutorService(EslEvent eslEvent) {
        if (StringUtils.isEmpty(EslEventUtil.getUniqueId(eslEvent))) {
            return executorMap.get(ThreadLocalRandom.current().nextInt(0, threadNum));
        } else {
            int coreHash = EslEventUtil.getUniqueId(eslEvent).hashCode();
            return executorMap.get(Math.abs(coreHash % threadNum));
        }
    }

    @PreDestroy
    public void cleanup() {
        destroyThreadPool();
    }
}
