package com.och.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CloudConfigManager {

    private static final Map<String, CloudConfig> configs = new ConcurrentHashMap<>();

    // 加载配置（可从数据库或配置文件读取）
    public static void loadConfig(CloudConfig config) {
        configs.put(config.getPlatform(), config);
    }

    public static CloudConfig getConfig(String platform) {
        return configs.get(platform);
    }
}
