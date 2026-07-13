package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.ivr.domain.IvrExecution;
import com.och.ivr.mapper.IvrExecutionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * IVR 执行追踪服务
 * <p>
 * 记录呼叫在 IVR 流程中的执行轨迹，包括启动、节点变更、完成/终止等。
 * 支持按 callId 查询当前执行状态，为 WebSocket 实时推送和事后回溯提供数据。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IvrExecutionServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.ivr.mapper.IvrExecutionMapper, com.och.ivr.domain.IvrExecution> {

    private final IvrExecutionMapper executionMapper;

    /**
     * 启动 IVR 流程执行记录
     *
     * @param callId    呼叫ID
     * @param flowId    IVR流程ID
     * @param versionId 流程版本ID
     * @return 执行记录
     */
    public IvrExecution start(String callId, Long flowId, Long versionId) {
        IvrExecution execution = new IvrExecution();
        execution.setCallId(callId);
        execution.setFlowId(flowId);
        execution.setVersionId(versionId);
        execution.setState("RUNNING");
        execution.setCreatedAt(LocalDateTime.now());
        executionMapper.insert(execution);
        log.info("[IVR执行] 流程启动: callId={}, flowId={}, executionId={}", callId, flowId, execution.getId());
        return execution;
    }

    /**
     * 更新当前节点
     *
     * @param callId        呼叫ID
     * @param currentNodeId 当前所在节点ID
     */
    public void updateNode(String callId, String currentNodeId) {
        LambdaQueryWrapper<IvrExecution> wrapper = new LambdaQueryWrapper<IvrExecution>()
                .eq(IvrExecution::getCallId, callId)
                .eq(IvrExecution::getState, "RUNNING");
        IvrExecution execution = executionMapper.selectOne(wrapper);
        if (execution != null) {
            execution.setCurrentNodeId(currentNodeId);
            executionMapper.updateById(execution);
            log.debug("[IVR执行] 节点变更: callId={}, nodeId={}", callId, currentNodeId);
        }
    }

    /**
     * 完成 IVR 流程
     *
     * @param callId    呼叫ID
     * @param exitType  退出类型（COMPLETED/TRANSFERRED/HANGUP/VOICEMAIL/ERROR）
     */
    public void complete(String callId, String exitType) {
        LambdaQueryWrapper<IvrExecution> wrapper = new LambdaQueryWrapper<IvrExecution>()
                .eq(IvrExecution::getCallId, callId)
                .eq(IvrExecution::getState, "RUNNING");
        IvrExecution execution = executionMapper.selectOne(wrapper);
        if (execution != null) {
            execution.setState(exitType);
            execution.setEndedAt(LocalDateTime.now());
            executionMapper.updateById(execution);
            log.info("[IVR执行] 流程结束: callId={}, exitType={}, executionId={}", callId, exitType, execution.getId());
        }
    }

    /**
     * 查询当前活跃的 IVR 执行记录
     *
     * @param callId 呼叫ID
     * @return 执行记录，不存在返回 null
     */
    public IvrExecution getActive(String callId) {
        return executionMapper.selectOne(
                new LambdaQueryWrapper<IvrExecution>()
                        .eq(IvrExecution::getCallId, callId)
                        .eq(IvrExecution::getState, "RUNNING"));
    }
}