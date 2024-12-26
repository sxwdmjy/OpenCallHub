package com.och.api.listener;

import com.och.api.event.OchLogEvent;
import com.och.common.thread.ThreadFactoryImpl;
import com.och.common.utils.ThreadUtils;
import com.och.system.domain.entity.SysOperLog;
import com.och.system.service.ISysOperLogService;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 操作日志监听
 *
 * @author danmo
 * @date 2024-03-13 10:48
 **/
@Slf4j
@AllArgsConstructor
@Component
public class OperationLogListener implements ApplicationListener<OchLogEvent> {

    private ISysOperLogService iSysOperLogService;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 10L, MILLISECONDS, new ArrayBlockingQueue<>(2048), new ThreadFactoryImpl("OperationLogThread_"));

    @Override
    public void onApplicationEvent(OchLogEvent event) {
        executor.execute(() -> {
            SysOperLog operationLog = (SysOperLog) event.getSource();
            iSysOperLogService.save(operationLog);
        });
    }

    @Override
    public boolean supportsAsyncExecution() {
        return false;
    }

    @PreDestroy
    public void shutdown() {
        log.info("操作日志线程池开始注销");
        ThreadUtils.shutdownAndAwaitTermination(executor);
    }
}
