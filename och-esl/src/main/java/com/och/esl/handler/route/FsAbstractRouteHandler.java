package com.och.esl.handler.route;

import com.och.common.config.redis.RedisService;
import com.och.common.domain.CallInfo;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFsCallCacheService;
import com.och.system.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author danmo
 * @date 2023-11-10 17:19
 **/
@Slf4j
@Component
public abstract class FsAbstractRouteHandler {

    @Lazy
    @Autowired
    protected FsClient fsClient;


    @Autowired
    protected RedisService redisService;

    @Lazy
    @Autowired
    protected IFsCallCacheService fsCallCacheService;

    @Resource
    protected ISipAgentService iSipAgentService;

    @Autowired
    protected ICallSkillService iCallSkillService;

    @Autowired
    protected IFsSipGatewayService iFsSipGatewayService;

    @Autowired
    protected IVoiceFileService iVoiceFileService;

    public abstract void handler(String address, CallInfo callInfo, String uniqueId,  String routeValue);

    protected void saveCallInfo(CallInfo callInfo){
        fsCallCacheService.saveCallInfo(callInfo);
    }
}
