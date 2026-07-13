package com.och.file.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Netty 文件服务器配置
 * <p>绑定 file-server 配置前缀，支持自定义监听端口、线程数和最大内容长度。</p>
 */
@Configuration
@ConfigurationProperties(prefix = "file-server")
@Data
public class NettyConfig {
    private int port = 9527; // 监听端口
    private int bossThreads = 1; // boss线程
    private int workerThreads = 4; // worker线程
    private int maxContentLength = 16 * 1024 * 1024; // 最大内容长度
}
