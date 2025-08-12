package com.och.calltask.job;

import com.och.calltask.domain.entity.CallTask;
import com.och.calltask.handler.CallTaskHandler;
import com.och.calltask.service.ICallTaskService;
import com.och.common.enums.CallTaskStatusEnum;
import com.och.common.enums.TaskTypeEnum;
import com.och.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 外呼任务
 *
 * @author danmo
 * @date 2025/06/25
 */
@RequiredArgsConstructor
@Slf4j
@Component
@DisallowConcurrentExecution
public class PredictiveDialerJob extends QuartzJobBean {

    private final ICallTaskService callTaskService;

    private final Map<String, CallTaskHandler> callTaskHandlerMap;


    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String taskId = context.getJobDetail().getJobDataMap().getString("taskId");
        log.info("开始执行预测外呼任务,任务ID：{}", taskId);
        try {
            CallTask callTask = callTaskService.getById(taskId);
            if (Objects.isNull(callTask)) {
                log.info("任务不存在,任务ID：{}", taskId);
                return;
            }
            if (CallTaskStatusEnum.NOT_START.getCode().equals(callTask.getStatus())) {
                callTaskService.updateStatus(Long.valueOf(taskId), CallTaskStatusEnum.PROCESSING);
            }
            if (CallTaskStatusEnum.END.getCode().equals(callTask.getStatus())) {
                log.info("任务已结束,任务ID：{}", taskId);
                return;
            }
            if (CallTaskStatusEnum.PAUSE.getCode().equals(callTask.getStatus())) {
                log.info("任务已暂停,任务ID：{}", taskId);
                return;
            }
            String handler = TaskTypeEnum.getHandler(callTask.getType());
            CallTaskHandler callTaskHandler = callTaskHandlerMap.get(handler);
            callTaskHandler.execute(Long.valueOf(taskId));
        } catch (Exception e) {
            log.error("任务执行异常 taskId:{}", taskId, e);
            throw new JobExecutionException("任务执行异常 taskId:" + taskId, e);
        } finally {
            Date nextFireTime = context.getNextFireTime();
            if (Objects.isNull(nextFireTime) && StringUtils.isNotBlank(taskId)) {
                log.info("任务已结束,任务ID：{}", taskId);
                callTaskService.updateStatus(Long.valueOf(taskId), CallTaskStatusEnum.END);
            }
        }

    }

}
