package com.och.ivr.handler.node;

import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.ivr.service.IFlowNodesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * 应答节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@Component
public class IFlowAnswerHandler extends AbstractIFlowNodeHandler {


    public IFlowAnswerHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowNodesService iFlowNodesService, IFlowEdgesService iFlowEdgesService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowNodesService, iFlowEdgesService, iFlowInfoService, iFlowInstancesService, fsClient);
    }

    @Override
    public void execute(FlowDataContext flowData) {
        log.info("应答节点处理 flowData：{}", flowData);
        try {
            fsClient.answer(flowData.getAddress(), flowData.getUniqueId());
            Long nextNodeId = getNextNodeId(flowData, "success");
            iFlowNoticeService.notice(2, ""+nextNodeId, flowData);
        } catch (Exception e) {
            throw new FlowNodeException(e);
        }
    }
}
