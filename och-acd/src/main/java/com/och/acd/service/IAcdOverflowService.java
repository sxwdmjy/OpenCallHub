package com.och.acd.service;
public interface IAcdOverflowService {
    boolean isQueueFull(Long skillGroupId);
    int processTimeoutOverflow(Long skillGroupId);
}
