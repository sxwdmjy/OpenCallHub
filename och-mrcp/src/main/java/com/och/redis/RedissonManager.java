package com.och.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonManager {

    private static volatile RedissonClient redissonClient;

    private RedissonManager() {} // 防止实例化

    public static RedissonClient getRedisson() {
        if (redissonClient == null) {
            synchronized (RedissonManager.class) {
                if (redissonClient == null) {
                    Config config = new Config();
                    // 单节点模式配置（根据实际情况选择模式）
                    config.useSingleServer()
                            .setAddress("redis://127.0.0.1:6379")
                            .setPassword("123456")
                            .setDatabase(0)
                            .setConnectionMinimumIdleSize(5)
                            .setConnectionPoolSize(10);
                    redissonClient = Redisson.create(config);

                    // 添加JVM关闭钩子
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (redissonClient != null && !redissonClient.isShutdown()) {
                            redissonClient.shutdown();
                        }
                    }));
                }
            }
        }
        return redissonClient;
    }

    public static void shutdown() {
        if (redissonClient != null && !redissonClient.isShutdown()) {
            redissonClient.shutdown();
        }
    }
}
