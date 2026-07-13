package com.och.infra.file;

import java.io.InputStream;

/**
 * 文件存储接口
 * <p>定义文件上传、下载、删除、访问URL生成和存在性判断的契约。
 * 支持本地文件系统和 MinIO 等多种实现，通过配置 och.file.type 切换。</p>
 *
 * @author danmo
 */
public interface FileStorage {

    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param fileName    原始文件名
     * @param contentType MIME 类型
     * @param size        文件大小（字节）
     * @return 文件唯一标识 key
     */
    String upload(InputStream inputStream, String fileName, String contentType, long size);

    /**
     * 下载文件
     *
     * @param fileKey 文件标识 key
     * @return 文件输入流
     */
    InputStream download(String fileKey);

    /**
     * 删除文件
     *
     * @param fileKey 文件标识 key
     */
    void delete(String fileKey);

    /**
     * 获取文件访问 URL
     *
     * @param fileKey 文件标识 key
     * @return 可访问的文件 URL
     */
    String getAccessUrl(String fileKey);

    /**
     * 判断文件是否存在
     *
     * @param fileKey 文件标识 key
     * @return true 表示文件存在
     */
    boolean exists(String fileKey);
}
