package com.och.system.service.impl;

import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.file.service.IFileUploadService;
import com.och.system.mapper.SysFileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SysFileService 单元测试
 * <p>验证文件上传服务逻辑，包括 MultipartFile 和 File 两种上传方式及异常处理。</p>
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class SysFileServiceTest {

    @Mock
    private SysFileMapper mapper;

    @Mock
    private IFileUploadService fileUploadService;

    @Mock
    private SysSettingConfig settingConfig;

    private SysFileServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SysFileServiceImpl(fileUploadService, settingConfig);
        setFieldInHierarchy(service, "baseMapper", mapper);
    }

    /** Walk up the class hierarchy to find and set a declared field. */
    static void setFieldInHierarchy(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    // ===================== uploadFile(MultipartFile) =====================

    @Test
    void uploadMultipartFile_shouldUploadAndSave() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt",
                "text/plain", "hello".getBytes());
        FileUploadVo vo = new FileUploadVo();
        vo.setFilePath("/upload/test.txt");
        vo.setCosId("cos-123");
        vo.setFileName("test.txt");
        vo.setFileSize("5");
        vo.setFileSuffix("txt");
        vo.setFileType(3);

        when(fileUploadService.fileUpload(any(MultipartFile.class), eq("local"))).thenReturn(vo);
        when(mapper.insert(any())).thenReturn(1);

        FileUploadVo result = service.uploadFile(file, "local");

        assertNotNull(result);
        assertEquals("/upload/test.txt", result.getFilePath());
        assertEquals("cos-123", result.getCosId());
        assertEquals("test.txt", result.getFileName());
        verify(fileUploadService).fileUpload(file, "local");
        verify(mapper).insert(any());
    }

    @Test
    void uploadMultipartFile_shouldThrowFileException_whenUploadFails() {
        MultipartFile file = new MockMultipartFile("bad.txt", "bad.txt",
                "text/plain", "data".getBytes());
        when(fileUploadService.fileUpload(any(MultipartFile.class), eq("local")))
                .thenThrow(new RuntimeException("upload failed"));

        assertThrows(FileException.class, () -> service.uploadFile(file, "local"));

        verify(mapper, never()).insert(any());
    }

    // ===================== uploadFile(File) =====================

    @Test
    void uploadFile_shouldUploadAndSave() {
        File file = new File("/tmp/test.pdf");
        FileUploadVo vo = new FileUploadVo();
        vo.setFilePath("/upload/test.pdf");
        vo.setCosId("cos-456");
        vo.setFileName("test.pdf");
        vo.setFileSize("1024");
        vo.setFileSuffix("pdf");
        vo.setFileType(3);

        when(fileUploadService.fileUpload(any(File.class), eq("ali"))).thenReturn(vo);
        when(mapper.insert(any())).thenReturn(1);

        FileUploadVo result = service.uploadFile(file, "ali");

        assertNotNull(result);
        assertEquals("/upload/test.pdf", result.getFilePath());
        verify(fileUploadService).fileUpload(file, "ali");
        verify(mapper).insert(any());
    }

    @Test
    void uploadFile_shouldThrowFileException_whenUploadFails() {
        File file = new File("/tmp/missing.txt");
        when(fileUploadService.fileUpload(any(File.class), eq("local")))
                .thenThrow(new RuntimeException("disk full"));

        assertThrows(FileException.class, () -> service.uploadFile(file, "local"));

        verify(mapper, never()).insert(any());
    }
}
