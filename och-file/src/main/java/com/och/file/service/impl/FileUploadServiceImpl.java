package com.och.file.service.impl;

import com.och.common.annotation.FileUploadType;
import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.exception.FileException;
import com.och.common.utils.StringUtils;
import com.och.file.handler.AbstractFileUploadHandler;
import com.och.file.service.IFileUploadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author danmo
 * @date 2023-11-01 15:15
 **/
@Slf4j
@Service
@AllArgsConstructor
public class FileUploadServiceImpl implements IFileUploadService, InitializingBean {

    private final SysSettingConfig lfsSettingConfig;
    private final List<AbstractFileUploadHandler> fileUploadHandlers;

    private final Map<String,AbstractFileUploadHandler> handlerTable = new ConcurrentHashMap<>(16);


    @Override
    public FileUploadVo fileUpload(MultipartFile file, String type) throws IOException {
        if(StringUtils.isEmpty(type)){
            type = lfsSettingConfig.getUploadType();
        }
        AbstractFileUploadHandler fileUploadHandler = handlerTable.get(type);
        if(Objects.isNull(fileUploadHandler)){
            throw new FileException("未知存储类型");
        }
        return fileUploadHandler.upload(file);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (AbstractFileUploadHandler fileUploadHandler : fileUploadHandlers) {
            FileUploadType type = fileUploadHandler.getClass().getAnnotation(FileUploadType.class);
            if (type == null) {
                type = fileUploadHandler.getClass().getSuperclass().getAnnotation(FileUploadType.class);
            }
            if (type == null || StringUtils.isEmpty(type.value())) {
                continue;
            }
            handlerTable.put(type.value(), fileUploadHandler);
        }
    }



}
