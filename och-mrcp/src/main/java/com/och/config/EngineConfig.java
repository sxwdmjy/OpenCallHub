package com.och.config;

import com.och.engine.CloudConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EngineConfig {

    private static final Config config = ConfigFactory.load("engine.conf");

    public static List<CloudConfig> getCloudConfigs() {
        List<? extends Config> cloudConfigs = config.getConfigList("engine.cloud");
        return cloudConfigs.stream()
                .map(c -> new CloudConfig(
                        c.getString("platform"),
                        c.getString("appKey"),
                        c.getString("apiKey"),
                        c.getString("apiSecret"),
                        c.getString("endpoint"),
                        c.getObject("customParams").entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().unwrapped().toString()))
                )).collect(Collectors.toList());
    }

}
