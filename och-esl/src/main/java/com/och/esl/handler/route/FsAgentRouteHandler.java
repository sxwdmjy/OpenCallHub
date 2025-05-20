package com.och.esl.handler.route;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.och.common.annotation.EslRouteName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import com.och.common.enums.RouteTypeEnum;
import com.och.common.utils.StringUtils;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import com.och.system.domain.vo.agent.SipAgentVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author danmo
 * @date 2023-11-10 17:20
 **/
@EslRouteName(RouteTypeEnum.AGENT)
@Component
@Slf4j
public class FsAgentRouteHandler extends FsAbstractRouteHandler {


    @Override
    public void handler(String address, CallInfo callInfo, String uniqueId, String agentId) {

        SipAgentVo sipAgent = iSipAgentService.getDetail(Long.valueOf(agentId));
        if(Objects.isNull(sipAgent)){
            log.error("转坐席未查询到坐席信息 callId:{}  callerNumber:{} calleeNumber:{},agentId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(),agentId);
            fsClient.hangupCall(address,callInfo.getCallId(),uniqueId);
            return;
        }

        String otherUniqueId = RandomUtil.randomNumbers(32);

        //坐席分机号码
        String calleeNumber = sipAgent.getAgentNumber();
        if(StringUtils.isEmpty(calleeNumber)){
            log.error("坐席未配置sip号码 callId:{}  callerNumber:{} calleeNumber:{},agentId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(),agentId);
            fsClient.hangupCall(address,callInfo.getCallId(),uniqueId);
            return;
        }
        log.info("转坐席 callId:{}, agent:{}", callInfo.getCallId(), agentId);

        callInfo.setCallee(calleeNumber);
        callInfo.setAgentId(sipAgent.getId());
        callInfo.setAgentName(sipAgent.getName());
        callInfo.setAgentNumber(sipAgent.getAgentNumber());

        //构建被叫通道
        ChannelInfo otherChannelInfo = ChannelInfo.builder().callId(callInfo.getCallId()).uniqueId(otherUniqueId).cdrType(1).type(1).directionType(2)
                .agentId(sipAgent.getId()).agentNumber(sipAgent.getAgentNumber()).agentName(sipAgent.getName())
                .callTime(DateUtil.current()).otherUniqueId(uniqueId)
                .called(calleeNumber).caller(callInfo.getCaller()).display(callInfo.getCallerDisplay()).build();
        callInfo.setChannelInfoMap(otherUniqueId,otherChannelInfo);
        callInfo.addUniqueIdList(otherUniqueId);
        callInfo.setProcess(ProcessEnum.CALL_BRIDGE);


        CallInfoDetail detail = new CallInfoDetail();
        detail.setCallId(callInfo.getCallId());
        detail.setStartTime(DateUtil.current());
        detail.setOrderNum(callInfo.getDetailList() == null ? 0 : callInfo.getDetailList().size() + 1);
        detail.setTransferType(1);

        FsSipGatewayQuery query = new FsSipGatewayQuery();
        query.setGatewayType(0);
        List<FsSipGateway> gatewayList = iFsSipGatewayService.getList(query);
        if(CollectionUtil.isNotEmpty(gatewayList)){
            fsClient.makeCall(address,callInfo.getCallId(), calleeNumber,callInfo.getCaller(),otherUniqueId,callInfo.getCalleeTimeOut(), gatewayList.get(0));
        }else {
            log.error("转坐席未查询到非外线网关 callId:{}  callerNumber:{} calleeNumber:{},agentId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(),agentId);
            fsClient.hangupCall(address,callInfo.getCallId(),uniqueId);
        }
        detail.setEndTime(DateUtil.current());
        callInfo.addDetailList(detail);
        fsCallCacheService.saveCallInfo(callInfo);
        fsCallCacheService.saveCallRel(otherUniqueId,callInfo.getCallId());
    }
}
