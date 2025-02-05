package com.och.engine;

public interface TtsEngine {


    /**
     * 提交文本合成任务
     *
     * @param text     输入文本
     * @param callback 异步回调（音频数据或错误）
     * @param config   平台配置
     */
    void synthesize(String text, TtsCallback callback, CloudConfig config);
}
