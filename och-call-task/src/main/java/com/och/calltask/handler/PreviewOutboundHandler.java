package com.och.calltask.handler;

import com.alibaba.fastjson2.JSONArray;
import com.och.calltask.domain.CallTaskRoundsConf;
import com.och.calltask.domain.entity.CallTask;
import com.och.calltask.service.ICallTaskService;
import com.och.common.config.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * 预览外呼任务处理器
 *
 * @author danmo
 * @date 2025/06/26
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class PreviewOutboundHandler implements CallTaskHandler {

    private final ICallTaskService callTaskService;
    private final RedisService redisService;

    @Override
    public void execute(Long taskId) {
        CallTask callTask = callTaskService.getById(taskId);
        if (Objects.isNull(callTask)) {
            log.info("任务不存在,任务ID：{}", taskId);
            return;
        }
        Long sourceId = callTask.getSourceId();
        if (Objects.isNull(sourceId)) {
            log.warn("数据源不存在,任务ID：{}, 数据源ID:{}", taskId, sourceId);
            return;
        }


        List<CallTaskRoundsConf> callTaskRoundsConfs = JSONArray.parseArray(callTask.getRoundsConf(), CallTaskRoundsConf.class);
        if (CollectionUtils.isEmpty(callTaskRoundsConfs)) {
            log.info("任务轮次配置为空,任务ID：{}, 数据源ID:{}", taskId, sourceId);
            return;
        }
        Integer currentRound = callTask.getCurrentRound();
        CallTaskRoundsConf callTaskRoundsConf = callTaskRoundsConfs.stream().filter(conf -> conf.getRounds().equals(currentRound)).findFirst().orElse(null);
        if (Objects.isNull(callTaskRoundsConf)) {
            log.info("任务轮次配置不存在,任务ID：{}, 数据源ID:{}, currentRound:{}", taskId, sourceId, currentRound);
            return;
        }


    }
}
