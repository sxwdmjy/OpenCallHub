package com.och.file.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.och.common.annotation.FileUploadType;
import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.common.utils.IpUtils;
import com.och.common.utils.MimeTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 本地
 *
 * @author danmo
 * @date 2023-11-01 15:16
 **/
@FileUploadType(value = "local")
@Slf4j
@Service
public class LocalFileUploadHandler extends AbstractFileUploadHandler {


    public LocalFileUploadHandler(SysSettingConfig lfsSettingConfig) {
        super(lfsSettingConfig);
    }

    @Override
    public FileUploadVo upload(MultipartFile multipartFile){
        String oldName = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(oldName);
        if(!checkFileFormat(suffix)){
            throw new FileException(String.format("%s文件格式不被允许上传",suffix));
        }
        Integer fileType = MimeTypeUtils.getFileType(suffix).getCode();
        String newPath = getFileTempPath(fileType);
        String uuid = IdUtil.fastSimpleUUID();
        String fileName = uuid +  "." + suffix;
        String saveUrl = newPath + fileName;
        // 保存本地，创建目录
        File file = new File(newPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File saveFile = new File(saveUrl);
        // 序列化文件到本地
        try {
            saveFile.createNewFile();
            multipartFile.transferTo(saveFile);
        } catch (IOException e) {
            throw new FileException(e.getMessage());
        }
        FileUploadVo fileUploadVo = new FileUploadVo();
        fileUploadVo.setCosId(uuid);
        fileUploadVo.setFileName(oldName);
        fileUploadVo.setFilePath(lfsSettingConfig.getLocalHost()+saveUrl);
        fileUploadVo.setFileSize(String.valueOf(multipartFile.getSize()));
        fileUploadVo.setFileSuffix(suffix);
        fileUploadVo.setFileType(MimeTypeUtils.getFileType(suffix).getCode());
        return fileUploadVo;
    }

    @Override
    public FileUploadVo upload(File uploadFile) {
        String oldName = uploadFile.getName();
        String suffix = FileUtil.getSuffix(oldName);
        if(!checkFileFormat(suffix)){
            throw new FileException(String.format("%s文件格式不被允许上传",suffix));
        }
        Integer fileType = MimeTypeUtils.getFileType(suffix).getCode();
        String newPath = getFileTempPath(fileType);
        String uuid = IdUtil.fastSimpleUUID();
        String fileName = uuid +  "." + suffix;
        String saveUrl = newPath + fileName;
        // 保存本地，创建目录
        File saveFile = FileUtil.touch(saveUrl);
        FileUtil.copy(uploadFile, saveFile, true);
        FileUploadVo fileUploadVo = new FileUploadVo();
        fileUploadVo.setCosId(uuid);
        fileUploadVo.setFileName(oldName);
        fileUploadVo.setFilePath(lfsSettingConfig.getLocalHost()+saveUrl);
        fileUploadVo.setFileSize(String.valueOf(FileUtil.size(uploadFile)));
        fileUploadVo.setFileSuffix(suffix);
        fileUploadVo.setFileType(MimeTypeUtils.getFileType(suffix).getCode());
        return fileUploadVo;
    }
}
