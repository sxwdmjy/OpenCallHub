package com.och.calltask.handler;

/**
 * 呼叫任务策略
 * @author danmo
 * @date 2025/06/25
 */
public interface CallTaskHandler {

    /**
     * 执行任务
     * @param taskId 任务ID
     */
    void execute(Long taskId);
}
