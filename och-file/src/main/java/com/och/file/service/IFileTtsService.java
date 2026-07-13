package com.och.file.service;

import java.io.File;
import java.util.function.Consumer;

/**
 * 文件 TTS（文字转语音）服务接口
 * <p>将文本内容转换为语音文件，支持多种第三方引擎（阿里云/腾讯云/讯飞等）。</p>
 */
public interface IFileTtsService {

    /**
     * 文字转语音
     * @param text         要转换的文本内容
     * @param type         引擎类型（1=腾讯, 2=阿里, 3=讯飞）
     * @param fileConsumer 语音文件生成后的回调
     */
    void textToSpeech(String text, Integer type, Consumer<File> fileConsumer);
}
