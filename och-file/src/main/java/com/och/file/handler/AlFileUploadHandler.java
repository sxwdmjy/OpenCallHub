package com.och.file.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.exceptions.ClientException;
import com.och.common.annotation.FileUploadType;
import com.och.common.config.oss.AliCloudConfig;
import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.common.utils.MimeTypeUtils;
import com.och.common.utils.StringUtils;
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
            uploadFile(saveUrl, file.getInputStream());
        } catch (ClientException | IOException e) {
            throw new FileException(e.getMessage());
        }
        FileUploadVo fileUploadVo = new FileUploadVo();
        fileUploadVo.setCosId(uuid);
        fileUploadVo.setFileName(oldName);
        fileUploadVo.setFilePath(aliCosConfig.getHost() + saveUrl);
        fileUploadVo.setFileSuffix(suffix);
        fileUploadVo.setFileSize(String.valueOf(file.getSize()));
        fileUploadVo.setFileType(fileType);
        return fileUploadVo;
    }

    @Override
    public FileUploadVo upload(File uploadFile) {
        String oldName = uploadFile.getName();
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
            Boolean aBoolean = uploadFile(saveUrl, inputStream);
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
        fileUploadVo.setFileSize(String.valueOf(uploadFile.length()));
        fileUploadVo.setFileType(fileType);
        return fileUploadVo;
    }


    private Boolean uploadFile(String url, InputStream inputStream) throws ClientException {
        AliCloudConfig.AliCosConfig aliCosConfig = lfsSettingConfig.getAliConfig().getCos();
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(aliCosConfig.getAccessKeyId(), aliCosConfig.getAccessKeySecret());
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        // 创建OSSClient实例。
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(aliCosConfig.getEndpoint())
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(aliCosConfig.getRegion())
                .build();
        try {
            // 创建PutObjectRequest对象。
            if(StringUtils.startsWith(url,"/")){
                url = url.substring(1);
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(aliCosConfig.getBucketName(), url, inputStream);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            log.error("上传阿里云存储异常 msg:{}, url:{}", oe.getMessage(), url, oe);
            return false;
        } finally {
            ossClient.shutdown();
        }
        return true;
    }
}
