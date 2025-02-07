package com.och.engine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CloudConfigManager {

    private static final Map<String, CloudConfig> configs = new ConcurrentHashMap<>();

    // 加载配置（可从数据库或配置文件读取）
    public static void loadConfig(List<CloudConfig> configs) {
        configs.stream().collect(Collectors.toMap(CloudConfig::getPlatform, config -> config, (config1, config2) -> config1));
    }

    public static CloudConfig getConfig(String platform) {
        return configs.get(platform);
    }
}
