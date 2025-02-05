package com.och.engine;

public interface AsrEngine {

    /**
     * 提交语音识别任务
     * @param audioData 输入的音频数据
     * @param callback 异步回调（识别文本或错误）
     * @param config 平台配置
     */
    void recognize(byte[] audioData, AsrCallback callback, CloudConfig config);
}
