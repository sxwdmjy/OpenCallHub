package com.och.engine;

public interface AsrEngine {


    default void start() {

    }

    default void end() {

    }
    /**
     * 提交语音识别任务
     *
     * @param audioData 输入的音频数据
     */
    void recognize(byte[] audioData);
}
