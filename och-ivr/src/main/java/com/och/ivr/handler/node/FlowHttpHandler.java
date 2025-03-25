package com.och.ivr.handler.node;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * HTTP节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */

@Slf4j
@Component("FlowHttpHandler")
public class FlowHttpHandler extends  AbstractIFlowNodeHandler{

    public FlowHttpHandler(RedisStateMachinePersister<Object, Object> persister, IFsCallCacheService fsCallCacheService, IFlowNoticeService iFlowNoticeService, IFlowInfoService iFlowInfoService, IFlowInstancesService iFlowInstancesService, FsClient fsClient, RedisService redisService) {
        super(persister, fsCallCacheService, iFlowNoticeService, iFlowInfoService, iFlowInstancesService, fsClient, redisService);
    }

    @Override
    public void execute(FlowDataContext flowData) throws FlowNodeException {
        log.info("满意度节点处理 flowData：{}", flowData);
        iFlowNoticeService.notice(2, "next", flowData);
    }

    @Override
    public void businessHandler(String event, FlowDataContext flowData) throws FlowNodeException {

    }
}
