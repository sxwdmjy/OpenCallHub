package com.och.esl.handler.call;

import com.och.common.constant.SysSettingConfig;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.esl.client.FsClient;
import com.och.esl.factory.FsEslRouteFactory;
import com.och.esl.service.IFsCallCacheService;
import com.och.system.service.ICallInPhoneService;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class FsAbstractCallProcess {

    @Lazy
    @Autowired
    protected FsClient fsClient;

    @Autowired
    protected SysSettingConfig sysSettingConfig;

    @Autowired
    protected IFsCallCacheService lfsCallCacheService;

    @Autowired
    protected FsEslRouteFactory routeFactory;

    @Autowired
    protected ICallInPhoneService callInPhoneService;

    public abstract void handler(String address, EslEvent event, CallInfo callInfo, ChannelInfo lfsChannelInfo);


}
