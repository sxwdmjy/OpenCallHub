package com.och.engine;

@FunctionalInterface
public interface TtsCallback {
    void onComplete(byte[] audioData, Throwable error);
}
