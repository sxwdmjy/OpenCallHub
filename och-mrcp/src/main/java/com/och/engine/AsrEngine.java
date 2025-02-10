package com.och.engine;

import com.och.mrcp.MrcpRequest;
import com.och.mrcp.MrcpSession;

public interface AsrEngine {


    default void start(MrcpRequest req, MrcpSession mrcpSession) {

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
