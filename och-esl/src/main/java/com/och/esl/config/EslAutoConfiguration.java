package com.och.esl.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ESL 模块自动配置
 * <p>启用 Spring 定时任务支持（供 ScheduledCleanupTask 使用），
 * 并确保 och-esl 模块的组件被自动扫描发现。</p>
 *
 * @author danmo
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = "com.och.esl")
public class EslAutoConfiguration {
}
