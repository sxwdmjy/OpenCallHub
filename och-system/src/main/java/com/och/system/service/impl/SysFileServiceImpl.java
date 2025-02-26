package com.och.system.service.impl;

import com.och.common.base.BaseServiceImpl;
import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.file.service.IFileUploadService;
import com.och.system.domain.entity.SysFile;
import com.och.system.mapper.SysFileMapper;
import com.och.system.service.ISysFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件管理(SysFile)表服务实现类
 *
 * @author danmo
 * @since 2024-11-18 10:22:33
 */
@Service
public class SysFileServiceImpl extends BaseServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    @Autowired
    private IFileUploadService fileUploadService;

    @Autowired
    private SysSettingConfig lfsSettingConfig;

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
        fileUploadVo.setId(sysFile.getId());
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

