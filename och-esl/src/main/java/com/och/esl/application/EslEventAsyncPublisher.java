package com.och.esl.application;

import com.och.esl.domain.NormalizedEslEvent;
import com.och.esl.application.EslEventAsyncPublisher;
import com.och.esl.application.EslEventListener;
import lombok.extern.slf4j.Slf4j;
import com.och.esl.application.EslEventAsyncPublisher;
import com.och.esl.application.EslEventListener;
import org.springframework.context.ApplicationEventPublisher;
import com.och.esl.application.EslEventAsyncPublisher;
import com.och.esl.application.EslEventListener;
import org.springframework.scheduling.annotation.Async;
import com.och.esl.application.EslEventAsyncPublisher;
import com.och.esl.application.EslEventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.och.esl.application.EslEventAsyncPublisher;
import com.och.esl.application.EslEventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ESL 事件异步发布器
 *
 * <p>将 Spring ApplicationEvent 的发布从 FreeSWITCH 回调线程剥离为异步，
 * 并通过 callId 哈希分区保证<strong>同一通话的事件顺序处理</strong>。</p>
 *
 * <h3>架构</h3>
 * <pre>
 * FreeSWITCH 回调线程
 *   → asyncPublish(event)                    [@Async → esl-event-* 线程池, 核心4/最大16]
 *     → partition = hash(callId) % 8
 *       → partitionExecutors[partition]       [单线程分区执行器, 保证同 callId 串行]
 *         → eventPublisher.publishEvent(event) [同步 Spring 事件发布]
 * </pre>
 *
 * <h3>分区策略</h3>
 * <p>使用 {@code Math.abs(callId.hashCode()) % PARTITION_COUNT} 将事件路由到固定分区。
 * 每个分区是单线程执行器，因此同一 callId 的所有事件会在同一线程上严格按到达顺序处理，
 * 避免 CALL_CREATE 在 CALL_ANSWER 之后到达下游等乱序问题。</p>
 *
 * <p>不同 callId 的事件可能分配到不同分区，实现<strong>跨通话并行处理</strong>
 * （最多 PARTITION_COUNT 个通话同时处理），提升吞吐量。</p>
 *
 * @author danmo
 * @see EslEventListener
 * @see com.och.esl.config.EslEventAsyncConfig
 */
@Slf4j
@Service
public class EslEventAsyncPublisher{

    /**
     * 分区数量，决定并发度和顺序保证粒度。
     * 8 个分区意味着最多 8 个通话的事件可并行处理，同一通话的事件串行。
     */
    private static final int PARTITION_COUNT = 8;

    /**
     * 每个分区的队列容量，总计 PARTITION_COUNT * 200 = 1600。
     */
    private static final int PARTITION_QUEUE_CAPACITY = 200;

    private final ApplicationEventPublisher eventPublisher;

    /** 分区执行器列表，每个执行器为单线程，保证同分区事件顺序 */
    private final List<ThreadPoolTaskExecutor> partitionExecutors;

    public EslEventAsyncPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.partitionExecutors = new ArrayList<>(PARTITION_COUNT);

        for (int i = 0; i < PARTITION_COUNT; i++) {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(1);
            executor.setMaxPoolSize(1);
            executor.setQueueCapacity(PARTITION_QUEUE_CAPACITY);
            executor.setThreadNamePrefix("esl-pub-" + i + "-");
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(10);
            executor.initialize();
            partitionExecutors.add(executor);
        }

        log.info("[EslEventAsyncPublisher] 初始化完成: {}个分区执行器, 总队列容量={}",
                PARTITION_COUNT, PARTITION_COUNT * PARTITION_QUEUE_CAPACITY);
    }

    /**
     * 异步发布 ESL 事件
     *
     * <p>此方法通过 {@code @Async("eslEventTaskExecutor")} 在专用线程池中执行，
     * 不阻塞 FreeSWITCH 回调线程。内部通过 callId 哈希分区进一步确保
     * 同一通话的事件顺序处理。</p>
     *
     * @param event 标准化后的 ESL 事件
     */
    @Async("eslEventTaskExecutor")
    public void asyncPublish(NormalizedEslEvent event) {
        String callId = event.getCallId();

        // 无 callId 的事件（理论上在 EslEventListener 中已被过滤，此处兜底）
        if (callId == null || callId.isBlank()) {
            log.debug("[EslEventAsyncPublisher] callId 为空, 同步发布: type={}", event.getEventType());
            eventPublisher.publishEvent(event);
            return;
        }

        int partition = Math.abs(callId.hashCode()) % PARTITION_COUNT;
        partitionExecutors.get(partition).submit(() -> {
            try {
                eventPublisher.publishEvent(event);
            } catch (Exception e) {
                log.error("[EslEventAsyncPublisher] 事件发布异常: callId={}, type={}",
                        callId, event.getEventType(), e);
            }
        });
    }

    /**
     * 获取各分区队列当前深度（用于监控/告警）
     *
     * @return 各分区队列大小数组，索引对应分区号
     */
    public int[] partitionQueueSizes() {
        int[] sizes = new int[PARTITION_COUNT];
        for (int i = 0; i < PARTITION_COUNT; i++) {
            sizes[i] = partitionExecutors.get(i).getThreadPoolExecutor().getQueue().size();
        }
        return sizes;
    }

    @PreDestroy
    public void shutdown() {
        log.info("[EslEventAsyncPublisher] 正在关闭 {} 个分区执行器...", PARTITION_COUNT);
        for (int i = 0; i < PARTITION_COUNT; i++) {
            try {
                partitionExecutors.get(i).shutdown();
            } catch (Exception e) {
                log.warn("[EslEventAsyncPublisher] 关闭分区执行器 {} 失败: {}", i, e.getMessage());
            }
        }
        log.info("[EslEventAsyncPublisher] 分区执行器已全部关闭");
    }
}
