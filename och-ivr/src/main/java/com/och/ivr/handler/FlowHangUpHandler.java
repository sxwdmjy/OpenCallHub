package com.och.ivr.handler;

import com.och.common.constant.FlowDataContext;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.ivr.service.IFlowNodesService;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * 挂机节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowHangUpHandler extends AbstractIFlowNodeHandler {


    public FlowHangUpHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowNodesService iFlowNodesService, IFlowEdgesService iFlowEdgesService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowNodesService, iFlowEdgesService, iFlowInfoService, iFlowInstancesService, fsClient);
    }

    @Override
    public void execute(FlowDataContext flowData) {

    }
}
