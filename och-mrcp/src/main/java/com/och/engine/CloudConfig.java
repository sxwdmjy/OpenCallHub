package com.och.engine;

import com.och.config.EngineConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class CloudConfig {

    private String platform;      // 平台名称，如 "aliyun", "tencent", "azure"
    private String appKey;        // 应用密钥
    private String apiKey;        // API密钥
    private String apiSecret;     // API密钥（可选）
    private String endpoint;      // 服务端点URL
    private Map<String, String> customParams; // 平台特有参数




}
