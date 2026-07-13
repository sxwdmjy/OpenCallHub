package com.och.infra.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置属性
 * <p>对应 application.yml 中 och.file 前缀的配置项，支持本地存储和 MinIO 两种模式。</p>
 *
 * @author danmo
 */
@Data
@ConfigurationProperties(prefix = "och.file")
public class FileStorageProperties {

    private String type = "local"; // local, minio
    private String localBasePath = "./data/files";
    private String minioEndpoint;
    private String minioAccessKey;
    private String minioSecretKey;
    private String minioBucket = "och-files";
}
