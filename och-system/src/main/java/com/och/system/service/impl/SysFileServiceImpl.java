package com.och.system.service.impl;

import com.och.common.base.BaseServiceImpl;
import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.file.service.IFileUploadService;
import com.och.system.domain.entity.SysFile;
import com.och.system.mapper.SysFileMapper;
import com.och.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件管理服务
 * <p>负责文件的上传与存储管理，支持MultipartFile和File两种上传方式</p>
 *
 * @author danmo
 * @since 2024-11-18 10:22:33
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysFileServiceImpl extends BaseServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    private final IFileUploadService fileUploadService;

    private final SysSettingConfig settingConfig;

    @Override
    public FileUploadVo uploadFile(MultipartFile file, String type) {
        FileUploadVo fileUploadVo;
        try {
            fileUploadVo = fileUploadService.fileUpload(file, type);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new FileException("文件上传失败");
        }
        SysFile sysFile = new SysFile();
        sysFile.setFilePath(fileUploadVo.getFilePath());
        sysFile.setCosId(fileUploadVo.getCosId());
        sysFile.setFileName(fileUploadVo.getFileName());
        sysFile.setFileSize(fileUploadVo.getFileSize());
        sysFile.setFileSuffix(fileUploadVo.getFileSuffix());
        sysFile.setFileType(fileUploadVo.getFileType());
        save(sysFile);
        return fileUploadVo;
    }

    @Override
    public FileUploadVo uploadFile(File file, String type) {
        FileUploadVo fileUploadVo;
        try {
            fileUploadVo = fileUploadService.fileUpload(file, type);
        } catch (Exception e) {
            throw new FileException("文件上传失败");
        }
        SysFile sysFile = new SysFile();
        sysFile.setFilePath(fileUploadVo.getFilePath());
        sysFile.setCosId(fileUploadVo.getCosId());
        sysFile.setFileName(fileUploadVo.getFileName());
        sysFile.setFileSize(fileUploadVo.getFileSize());
        sysFile.setFileSuffix(fileUploadVo.getFileSuffix());
        sysFile.setFileType(fileUploadVo.getFileType());
        save(sysFile);
        fileUploadVo.setId(sysFile.getId());
        return fileUploadVo;
    }
}

