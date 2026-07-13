package com.och.calltask.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.calltask.domain.OutboundTask;
import java.util.Map;
public interface IOutboundTaskService {
    OutboundTask createTask(OutboundTask task);
    OutboundTask submitTask(Long taskId);
    OutboundTask startTask(Long taskId);
    OutboundTask pauseTask(Long taskId);
    OutboundTask cancelTask(Long taskId);
    IPage<OutboundTask> listTasks(int pageNum, int pageSize, String keyword);
    OutboundTask getTask(Long id);
    OutboundTask updateTaskConfig(Long taskId, Map<String, Object> roundsConf);
    Map<String, Object> getTaskProgress(Long taskId);
}
