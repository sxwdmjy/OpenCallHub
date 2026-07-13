package com.och.infra.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 已存储文件信息
 * <p>封装文件上传后的元数据，包括文件唯一标识、原始名称、MIME 类型、大小、访问 URL 和创建时间。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {

    private String fileKey;
    private String originalName;
    private String contentType;
    private Long size;
    private String accessUrl;
    private Instant createdAt;
}
