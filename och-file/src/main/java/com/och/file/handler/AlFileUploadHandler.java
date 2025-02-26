package com.och.file.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.exceptions.ClientException;
import com.och.common.annotation.FileUploadType;
import com.och.common.config.oss.AliCloudConfig;
import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.common.utils.MimeTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 阿里
 *
 * @author danmo
 * @date 2023-11-01 15:16
 **/
@FileUploadType(value = "ali")
@Slf4j
@Service
public class AlFileUploadHandler extends AbstractFileUploadHandler {


    public AlFileUploadHandler(SysSettingConfig lfsSettingConfig) {
        super(lfsSettingConfig);
    }

    @Override
    public FileUploadVo upload(MultipartFile file) {

        String oldName = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(oldName);
        if (!checkFileFormat(suffix)) {
            throw new FileException(String.format("%s文件格式不被允许上传", suffix));
        }
        Integer fileType = MimeTypeUtils.getFileType(suffix).getCode();
        String newPath = getFileTempPath(fileType);
        String uuid = IdUtil.fastSimpleUUID();
        String fileName = uuid + "." + suffix;
        String saveUrl = newPath + fileName;
        AliCloudConfig.AliCosConfig aliCosConfig = lfsSettingConfig.getAliConfig().getCos();
        try {
            uploadFile(saveUrl, file.getInputStream(), aliCosConfig.getHost(), aliCosConfig.getBucketName());
        } catch (ClientException | IOException e) {
            throw new FileException(e.getMessage());
        }
        FileUploadVo fileUploadVo = new FileUploadVo();
        fileUploadVo.setCosId(uuid);
        fileUploadVo.setFileName(oldName);
        fileUploadVo.setFilePath(aliCosConfig.getHost() + saveUrl);
        fileUploadVo.setFileSuffix(suffix);
        fileUploadVo.setFileType(fileType);
        return fileUploadVo;
    }

    @Override
    public FileUploadVo upload(File uploadFile) {
        String oldName = uploadFile.getAbsolutePath();
        String suffix = FileUtil.getSuffix(oldName);
        if (!checkFileFormat(suffix)) {
            throw new FileException(String.format("%s文件格式不被允许上传", suffix));
        }
        Integer fileType = MimeTypeUtils.getFileType(suffix).getCode();
        String newPath = getFileTempPath(fileType);
        String uuid = IdUtil.fastSimpleUUID();
        String fileName = uuid + "." + suffix;
        String saveUrl = newPath + fileName;
        AliCloudConfig.AliCosConfig aliCosConfig = lfsSettingConfig.getAliConfig().getCos();
        InputStream inputStream = FileUtil.getInputStream(uploadFile);
        try {
            Boolean aBoolean = uploadFile(saveUrl, inputStream, aliCosConfig.getHost(), aliCosConfig.getBucketName());
            if (!aBoolean) {
                throw new FileException("上传阿里云存储异常");
            }
        } catch (ClientException | FileException e) {
            throw new FileException(e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("上传阿里云存储异常 msg:{}", e.getMessage(), e);
            }
        }
        FileUploadVo fileUploadVo = new FileUploadVo();
        fileUploadVo.setCosId(uuid);
        fileUploadVo.setFileName(oldName);
        fileUploadVo.setFilePath(aliCosConfig.getHost() + saveUrl);
        fileUploadVo.setFileSuffix(suffix);
        fileUploadVo.setFileType(fileType);
        return fileUploadVo;
    }


    private Boolean uploadFile(String url, InputStream inputStream, String endpoint, String bucketName) throws ClientException {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, url, inputStream);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            log.error("上传阿里云存储异常 msg:{}, url:{}", oe.getMessage(), url, oe);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return true;
    }
}
