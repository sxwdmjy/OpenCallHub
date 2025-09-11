package com.och.calltask.service.impl;


import com.och.calltask.job.PredictiveDialerJob;
import com.och.calltask.service.IPredictiveDialerService;
import com.och.common.exception.CommonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Calendar;
import java.util.stream.Collectors;

/**
 * 外呼任务
 *
 * @author: danmo
 * @date: 2025/06/25
 */

@RequiredArgsConstructor
@Slf4j
@Service
public class PredictiveDialerServiceImpl implements IPredictiveDialerService {

    private final Scheduler scheduler;

    private static final String GROUP_NAME = "CALL_TASK_GROUP";
    private static final String JOB_NAME = "CALL_TASK_JOB_";
    private static final String TRIGGER_NAME = "CALL_TASK_TRIGGER_";

    @Override
    public void createTask(Long taskId, Integer priority, Date startDay, Date endDay, String sTime, String eTime, String workCycle) {
        log.info("开始创建外呼任务 taskId:{}, startDay:{}, endDay:{}, stime:{}, etime:{}, workCycle:{}", taskId, startDay, endDay, sTime, eTime, workCycle);
        try {
            JobKey jobKey = JobKey.jobKey(JOB_NAME + taskId, GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                log.warn("任务 {} 已存在，正在删除旧任务", taskId);
                scheduler.deleteJob(jobKey);
            }
            // 创建任务详情
            JobDetail jobDetail = JobBuilder.newJob(PredictiveDialerJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("taskId", taskId.toString())
                    .storeDurably()
                    .build();

            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_NAME + taskId, GROUP_NAME)
                    .startAt(parseStartDay(startDay))
                    .endAt(parseEndDay(endDay))
                    .withPriority(priority);
            int startHour = parseTime(sTime).getHour();
            int startMinute = parseTime(sTime).getMinute();
            int endHour = parseTime(eTime).getHour();
            int endMinute = parseTime(eTime).getMinute();
            DailyTimeIntervalScheduleBuilder scheduleBuilder = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                    .startingDailyAt(TimeOfDay.hourAndMinuteOfDay(startHour, startMinute))
                    .endingDailyAt(TimeOfDay.hourAndMinuteOfDay(endHour, endMinute))
                    .onDaysOfTheWeek(parseWeekDays(workCycle))
                    // 每分钟执行
                    .withIntervalInMinutes(1);
            DailyTimeIntervalTrigger trigger = triggerBuilder.withSchedule(scheduleBuilder)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("成功创建外呼任务 taskId:{}", taskId);
        } catch (Exception e) {
            log.error("创建任务失败 taskId:{}", taskId, e);
            throw new CommonException("创建任务异常");
        }
    }

    @Override
    public void deleteTask(Long taskId) {
        log.info("开始删除外呼任务 taskId:{}", taskId);
        try {
            JobKey jobKey = JobKey.jobKey(JOB_NAME + taskId, GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("成功删除外呼任务 taskId:{}", taskId);
            } else {
                log.warn("任务 {} 不存在", taskId);
            }
        } catch (Exception e) {
            log.error("删除任务失败 taskId:{}", taskId, e);
            throw new CommonException("删除任务异常");
        }
    }

    @Override
    public void deleteTask(List<Long> ids) {
        log.info("开始批量删除外呼任务 taskIds:{}", ids);
        ids.forEach(this::deleteTask);
    }

    @Override
    public void pauseTask(Long taskId) {
        log.info("开始暂停外呼任务 taskId:{}", taskId);
        try {
            JobKey jobKey = JobKey.jobKey(JOB_NAME + taskId, GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
                log.info("成功暂停外呼任务 taskId:{}", taskId);
            } else {
                log.info("任务 {} 不存在", taskId);
            }
        } catch (Exception e) {
            log.error("暂停任务失败 taskId:{}", taskId, e);
            throw new CommonException("暂停任务异常");
        }
    }

    @Override
    public void resumeTask(Long taskId) {
        log.info("开始恢复外呼任务 taskId:{}", taskId);
        try {
            JobKey jobKey = JobKey.jobKey(JOB_NAME + taskId, GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
            } else {
                log.info("任务 {} 不存在", taskId);
            }
        } catch (Exception e) {
            log.error("恢复任务失败 taskId:{}", taskId, e);
            throw new CommonException("恢复任务异常");
        }
    }

    /**
     * 解析开始时间
     *
     * @param day
     * @return
     */
    private Date parseStartDay(Date day) {
        if (day == null) {
            throw new IllegalArgumentException("日期不能为空");
        }

        Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(day);
        // 设置为当天的00:00:00.000
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 解析结束时间
     *
     * @param day
     * @return
     */
    private Date parseEndDay(Date day) {
        if (day == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        // 设置为当天的23:59:59.999
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 解析时间
     *
     * @param timeStr
     * @return
     */
    private LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * @param cycle
     * @return
     */
    private Set<Integer> parseWeekDays(String cycle) {
        return Arrays.stream(cycle.split(","))
                .map(Integer::parseInt)
                .map(weekDay -> {
                    return switch (weekDay) {
                        case 1 -> Calendar.MONDAY;
                        case 2 -> Calendar.TUESDAY;
                        case 3 -> Calendar.WEDNESDAY;
                        case 4 -> Calendar.THURSDAY;
                        case 5 -> Calendar.FRIDAY;
                        case 6 -> Calendar.SATURDAY;
                        case 7 -> Calendar.SUNDAY;
                        default -> null;
                    };
                })
                .collect(Collectors.toSet());
    }
}
