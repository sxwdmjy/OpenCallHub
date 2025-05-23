package com.och.file.service;

import com.och.common.domain.file.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author danmo
 * @date 2023-11-01 15:15
 **/
public interface IFileUploadService {

    /**
     * @param file 文件
     * @param type 上传方式 local ali  tx
     * @return
     */
    FileUploadVo fileUpload(MultipartFile file, String type);


    FileUploadVo fileUpload(File file, String type);
}
