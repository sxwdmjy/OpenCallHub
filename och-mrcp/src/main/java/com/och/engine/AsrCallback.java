package com.och.engine;

@FunctionalInterface
public interface AsrCallback {
    void onComplete(String text, Throwable error);
}
