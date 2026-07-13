package com.och.file.service.impl;

import com.och.common.annotation.FileTtsType;
import com.och.common.constant.SysSettingConfig;
import com.och.common.utils.StringUtils;
import com.och.file.handler.AbstractFileTtsHandler;
import com.och.file.service.IFileTtsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 文件 TTS 服务实现
 * <p>根据配置的引擎类型和 {@code @FileTtsType} 注解自动发现 TTS 处理器，
 * 将文本转换为语音文件，支持阿里云、腾讯云等平台。</p>
 */
@AllArgsConstructor
@Slf4j
@Service
public class FileTtsServiceImpl implements IFileTtsService, InitializingBean {

    private final SysSettingConfig lfsSettingConfig;
    private final List<AbstractFileTtsHandler> fileTtsHandlers;
    private final Map<String, AbstractFileTtsHandler> handlerTable = new ConcurrentHashMap<>(16);

    @Override
    public void textToSpeech(String text, Integer type, Consumer<File> fileConsumer) {
        String typeStr = lfsSettingConfig.getTtsType();
        if (Objects.nonNull(type)) {
            switch (type) {
                case 1 -> typeStr = "tx";
                case 2 -> typeStr = "ali";
                case 3 -> typeStr = "xf";
                default -> typeStr = "ali";
            }
        }
        AbstractFileTtsHandler fileTtsHandler = handlerTable.get(typeStr);
        fileTtsHandler.tts(text, fileConsumer);

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (AbstractFileTtsHandler fileTtsHandler : fileTtsHandlers) {
            FileTtsType type = fileTtsHandler.getClass().getAnnotation(FileTtsType.class);
            if (type == null) {
                type = fileTtsHandler.getClass().getSuperclass().getAnnotation(FileTtsType.class);
            }
            if (type == null || StringUtils.isEmpty(type.value())) {
                continue;
            }
            handlerTable.put(type.value(), fileTtsHandler);
        }
    }

}
