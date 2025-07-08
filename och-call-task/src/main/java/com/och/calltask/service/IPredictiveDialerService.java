package com.och.calltask.service;


import java.util.Date;
import java.util.List;

/**
 * 外呼任务
 * @author danmo
 * @date 2026/06/25
 */
public interface IPredictiveDialerService {

    /**
     * 创建定时任务
     *
     * @param taskId 任务ID
     * @param startDay 任务开始时间
     * @param endDay 任务结束时间
     * @param sTime 每天开始时间
     * @param eTime 每天结束时间
     * @param workCycle 周期时间
     */
    void createTask(Long taskId, Date startDay, Date endDay, String sTime, String eTime, String workCycle);

    /**
     * 删除定时任务
     *
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);
    /**
     * 批量删除定时任务
     *
     * @param ids ids
     */
    void deleteTask(List<Long> ids);

    /**
     * 暂停定时任务
     *
     * @param taskId 任务ID
     */
    void pauseTask(Long taskId);

    /**
     * 恢复定时任务
     *
     * @param taskId 任务ID
     */
    void resumeTask(Long taskId);


}
