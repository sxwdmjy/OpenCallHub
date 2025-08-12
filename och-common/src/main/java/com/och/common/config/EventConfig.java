package com.och.common.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.och.common.utils.ThreadPoolUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

@Configuration
public class EventConfig {

    @Bean(name = "applicationEventMulticaster")
    public SimpleApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(TtlExecutors.getTtlExecutor(ThreadPoolUtil.getApplicationEventThreadPool()));
        return eventMulticaster;
    }
}
