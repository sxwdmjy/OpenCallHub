package com.och.esl.handler.call;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.och.common.annotation.EslProcessName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.vo.route.CallRouteVo;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 *  呼叫另一条腿
 * @author danmo
 * @date 2023-10-23 17:05
 **/
@EslProcessName(ProcessEnum.CALL_OTHER)
@Component
@Slf4j
public class FsCallOtherProcess extends FsAbstractCallProcess {

    @Override
    public void handler(String address, EslEvent event, CallInfo callInfo, ChannelInfo lfsChannelInfo) {
        String filePath = sysSettingConfig.getFsProfile() + "/" + DateUtil.today() + "/" + callInfo.getCallId() + "_"  + DateUtil.current() + sysSettingConfig.getFsFileSuffix();
        //设置振铃录音
        fsClient.record(address,callInfo.getCallId(),lfsChannelInfo.getUniqueId(),filePath);
        callInfo.setRecord(filePath);
        callInfo.setRecordStartTime(lfsChannelInfo.getAnswerTime());
        String otherUniqueId = RandomUtil.randomNumbers(32);

        log.info("开始呼另外一条腿: callId:{}  display:{}  called:{}  uniqueId:{} ", callInfo.getCallId(), callInfo.getCalleeDisplay(), callInfo.getCallee(), otherUniqueId);
        callInfo.addUniqueIdList(otherUniqueId);
        String callee = callInfo.getCallee();
        CallRouteVo callRoute = lfsCallCacheService.getCallRoute(callInfo.getCallee(), callInfo.getDirection());
        if(Objects.isNull(callRoute)){
            log.info("CallOtherProcess 未配置号码路由 callee:{}",callee);
            fsClient.hangupCall(address, callInfo.getCallId(), lfsChannelInfo.getUniqueId());
            return;
        }
        if(Objects.isNull(callRoute.getRouteValueId())){
            log.info("CallOtherProcess 号码路由未关联网关信息 callee:{}",callee);
            fsClient.hangupCall(address, callInfo.getCallId(), lfsChannelInfo.getUniqueId());
            return;
        }
        //构建主叫通道
        ChannelInfo otherChannelInfo = ChannelInfo.builder().callId(callInfo.getCallId()).uniqueId(otherUniqueId).cdrType(2).type(2)
                .agentId(lfsChannelInfo.getAgentId()).agentNumber(lfsChannelInfo.getAgentNumber()).agentName(lfsChannelInfo.getAgentName())
                .callTime(DateUtil.current()).otherUniqueId(lfsChannelInfo.getUniqueId())
                .called(callee).caller(callInfo.getCaller()).display(callInfo.getCallerDisplay()).build();
        callInfo.setChannelInfoMap(otherUniqueId,otherChannelInfo);
        callInfo.setProcess(ProcessEnum.CALL_BRIDGE);
        lfsCallCacheService.saveCallInfo(callInfo);
        lfsCallCacheService.saveCallRel(otherUniqueId,callInfo.getCallId());
        FsSipGateway sipGateway = fsSipGatewayService.getDetail(callRoute.getRouteValueId());
        try {
            fsClient.makeCall(address,callInfo.getCallId(), callInfo.getCallee(),callInfo.getCalleeDisplay(),otherUniqueId,callInfo.getCalleeTimeOut(), sipGateway);
        } catch (Exception e) {
            log.info("CallOtherProcess 呼叫另一条腿异常 error:{}",e.getMessage(),e);
            fsClient.hangupCall(address, callInfo.getCallId(), lfsChannelInfo.getUniqueId());
        }
    }


}
