package com.och.infra.file;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储实现
 * <p>
 * 当配置 och.file.type=local 时激活（默认值），将文件存储到服务器本地磁盘。
 * 文件以 UUID + 原始文件名作为唯一标识。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "och.file", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final FileStorageProperties properties;

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType, long size) {
        try {
            Path basePath = getBasePath();
            Files.createDirectories(basePath);

            String fileKey = UUID.randomUUID().toString().replace("-", "") + "_" + sanitizeFileName(fileName);
            Path targetPath = resolveFilePath(fileKey);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[LocalFileStorage] File uploaded: key={}, size={}", fileKey, size);
            return fileKey;
        } catch (IOException e) {
            log.error("[LocalFileStorage] Upload failed: fileName={}", fileName, e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public InputStream download(String fileKey) {
        try {
            Path filePath = resolveFilePath(fileKey);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + fileKey);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("[LocalFileStorage] Download failed: key={}", fileKey, e);
            throw new RuntimeException("File download failed", e);
        }
    }

    @Override
    public void delete(String fileKey) {
        try {
            Path filePath = resolveFilePath(fileKey);
            Files.deleteIfExists(filePath);
            log.info("[LocalFileStorage] File deleted: key={}", fileKey);
        } catch (IOException e) {
            log.error("[LocalFileStorage] Delete failed: key={}", fileKey, e);
            throw new RuntimeException("File delete failed", e);
        }
    }

    @Override
    public String getAccessUrl(String fileKey) {
        resolveFilePath(fileKey);
        return "/api/file/download/" + URLEncoder.encode(fileKey, StandardCharsets.UTF_8);
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            return Files.exists(resolveFilePath(fileKey));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Path resolveFilePath(String fileKey) {
        validateFileKey(fileKey);
        Path basePath = getBasePath();
        Path filePath = basePath.resolve(fileKey).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException("非法文件路径: " + fileKey);
        }
        return filePath;
    }

    private Path getBasePath() {
        return Paths.get(properties.getLocalBasePath()).toAbsolutePath().normalize();
    }

    private void validateFileKey(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            throw new IllegalArgumentException("文件标识不能为空");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "unnamed";
        }
        String normalized = fileName.replace('\\', '/');
        int lastSeparatorIndex = normalized.lastIndexOf('/');
        if (lastSeparatorIndex >= 0) {
            normalized = normalized.substring(lastSeparatorIndex + 1);
        }
        String safeName = normalized.replaceAll("[\\r\\n\\t/:*?\"<>|]+", "_").trim();
        return safeName.isEmpty() ? "unnamed" : safeName;
    }
}
