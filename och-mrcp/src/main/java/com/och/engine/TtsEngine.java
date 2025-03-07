package com.och.engine;

public interface TtsEngine {


    /**
     * 提交文本合成任务
     *
     * @param text     输入文本
     * @param voice     音色
     * @param callback 异步回调（音频数据或错误）
     */
    void synthesize(String text, String voice, TtsCallback callback);
}
