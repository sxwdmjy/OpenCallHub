package com.och.system;

import com.och.api.OchApiApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * API 应用上下文集成测试
 * <p>
 * 验证 Spring Boot 应用上下文可以完整加载。
 * 需要运行中的 MySQL + Redis + RabbitMQ 基础设施。
 * 在 CI 环境中通过环境变量 INTEGRATION_TEST=true 启用。
 * </p>
 *
 * @author danmo
 */
@SpringBootTest(classes = OchApiApplication.class)
@Disabled("需要运行中的 MySQL + Redis + RabbitMQ 基础设施，CI 环境中通过 INTEGRATION_TEST=true 启用")
class SystemApplicationTests {

    @Test
    void contextLoads() {
    }
}
