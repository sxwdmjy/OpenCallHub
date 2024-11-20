package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.common.domain.file.FileUploadVo;
import com.och.system.domain.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理(SysFile)表服务接口
 *
 * @author danmo
 * @since 2024-11-18 10:22:33
 */
public interface ISysFileService extends IBaseService<SysFile> {

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 文件信息
     */
    FileUploadVo uploadFile(MultipartFile file);
}

