package com.och.engine;

import com.alibaba.fastjson.JSONObject;

public interface TtsCallback {
    void onComplete(byte[] audioData, Throwable error);

    void onComplete(JSONObject result);
}
