package com.och.file.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.och.common.annotation.FileUploadType;
import com.och.common.config.oss.TxCosConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.common.utils.MimeTypeUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 腾讯
 *
 * @author danmo
 * @date 2023-11-01 15:16
 **/
@FileUploadType(value = "tx")
@Slf4j
@Service
public class TxFileUploadHandler extends AbstractFileUploadHandler {


    @Override
    public FileUploadVo upload(MultipartFile file) throws IOException {
        String oldName = file.getOriginalFilename();
        //获取扩展名，默认是wav
        String suffix = FileUtil.getSuffix(oldName);
        if (!checkFileFormat(suffix)) {
            throw new FileException(String.format("%s文件格式不被允许上传", suffix));
        }
        Integer fileType = MimeTypeUtils.getFileType(suffix).getCode();
        String newPath = getFileTempPath(fileType);
        String uuid = IdUtil.fastSimpleUUID();
        String fileName = uuid + "." + suffix;
        String saveUrl = newPath + fileName;
        TxCosConfig txCosConfig = lfsSettingConfig.getTxCosConfig();
        uploadFile(saveUrl, file.getInputStream(), txCosConfig.getAccessKey(), txCosConfig.getSecretKey(), txCosConfig.getRegionName(), txCosConfig.getBucketName());

        FileUploadVo fileUploadVo = new FileUploadVo();
        fileUploadVo.setCosId(uuid);
        fileUploadVo.setFileName(oldName);
        fileUploadVo.setFilePath(txCosConfig.getHost() + saveUrl);
        fileUploadVo.setFileSuffix(suffix);
        fileUploadVo.setFileType(MimeTypeUtils.getFileType(suffix).getCode());
        return fileUploadVo;
    }

    public static Boolean uploadFile(String url, InputStream inputStream, String accessKey, String secretKey, String regionName, String bucketName) {
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个threadpool, 若不传入线程池, 默认TransferManager中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosclient, threadPool);
        ObjectMetadata metadata = new ObjectMetadata();
        try {
            metadata.setContentLength(inputStream.available());
        } catch (IOException e) {
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, url, inputStream, metadata);
        try {
            // 返回一个异步结果Upload, 可同步的调用waitForUploadResult等待upload结束, 成功返回UploadResult, 失败抛出异常.
            long startTime = System.currentTimeMillis();
            Upload upload = transferManager.upload(putObjectRequest);
            //showTransferProgress(upload);
            UploadResult uploadResult = upload.waitForUploadResult();
            long endTime = System.currentTimeMillis();
            log.info("used time: " + (endTime - startTime) / 1000);
            log.info("requestId:{}", uploadResult.getRequestId());
            log.info(uploadResult.getKey());
        } catch (Exception e) {
            log.error("上传腾讯云存储异常 msg:{}, url:{}", e.getMessage(), url, e);
            return false;
        }
        transferManager.shutdownNow();
        cosclient.shutdown();
        return true;
    }
}
