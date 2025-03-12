package com.och.file.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file-server")
@Data
public class NettyConfig {
    private int port = 9527; // 监听端口
    private int bossThreads = 1; // boss线程
    private int workerThreads = 4; // worker线程
    private int maxContentLength = 16 * 1024 * 1024; // 最大内容长度
}
