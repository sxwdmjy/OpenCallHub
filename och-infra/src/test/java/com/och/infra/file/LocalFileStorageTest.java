package com.och.infra.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本地文件存储测试
 * <p>
 * 验证本地存储只允许访问配置的基础目录，防止带路径的文件名或 fileKey 绕过目录边界。
 * </p>
 *
 * @author danmo
 */
class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadSanitizesOriginalFileNameAndStoresUnderBasePath() throws Exception {
        LocalFileStorage fileStorage = localFileStorage();

        String fileKey = fileStorage.upload(new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)),
                "../dir/demo.txt", "text/plain", 5L);

        assertFalse(fileKey.contains("/"));
        assertFalse(fileKey.contains(".."));
        assertTrue(Files.exists(tempDir.resolve(fileKey)));
        assertEquals("hello", new String(fileStorage.download(fileKey).readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void rejectTraversalFileKey() {
        LocalFileStorage fileStorage = localFileStorage();

        assertThrows(IllegalArgumentException.class, () -> fileStorage.download("../secret.txt"));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.getAccessUrl("../secret.txt"));
        assertFalse(fileStorage.exists("../secret.txt"));
    }

    private LocalFileStorage localFileStorage() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setLocalBasePath(tempDir.toString());
        return new LocalFileStorage(properties);
    }
}
