package com.och.api.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.och.api.service.ICallService;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.DirectionEnum;
import com.och.common.enums.ProcessEnum;
import com.och.common.exception.CommonException;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFsCallCacheService;
import com.och.security.utils.SecurityUtils;
import com.och.system.domain.entity.CallDisplay;
import com.och.system.domain.entity.CallRecord;
import com.och.system.domain.entity.CorpInfo;
import com.och.system.domain.query.agent.SipAgentQuery;
import com.och.system.domain.query.call.CallQuery;
import com.och.system.domain.query.call.CallRecordQuery;
import com.och.system.domain.query.display.CallDisplayQuery;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.domain.vo.call.CallRecordVo;
import com.och.system.domain.vo.display.CallDisplayVo;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.service.ICallDisplayService;
import com.och.system.service.ICallRecordService;
import com.och.system.service.ICorpInfoService;
import com.och.system.service.ISipAgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ICallServiceImpl implements ICallService {

    @Autowired
    private FsClient fsClient;

    @Resource
    private ISipAgentService iSipAgentService;

    @Resource
    private ICallDisplayService iCallDisplayService;
    @Resource
    private ICorpInfoService corpInfoService;

    @Autowired
    private IFsCallCacheService iFsCallCacheService;

    @Autowired
    private ICallRecordService iCallRecordService;

    @Override
    public Long makeCall(CallQuery query) {
        if (Objects.isNull(query.getAgentId())) {
            throw new CommonException("坐席ID不能为空");
        }
        SipAgentQuery sipAgentQuery = new SipAgentQuery();
        sipAgentQuery.setId(query.getAgentId());
        List<SipAgentVo> agentList = iSipAgentService.getInfoByQuery(sipAgentQuery);

        if (CollectionUtil.isEmpty(agentList)) {
            throw new CommonException("未查询到有效坐席信息");
        }
        SipAgentVo sipAgent = agentList.get(0);
        Long callId = IdUtil.getSnowflakeNextId();
        String uniqueId = RandomUtil.randomNumbers(32);
        //构建呼叫总线
        CallInfo callInfo = CallInfo.builder().callId(callId)
                .agentId(sipAgent.getId()).agentNumber(sipAgent.getAgentNumber()).agentName(sipAgent.getName())
                .caller(sipAgent.getAgentNumber()).callee(query.getCallee()).direction(DirectionEnum.OUTBOUND.getType())
                .callTime(DateUtil.current()).hiddenCustomer(query.getHiddenCustomer())
                .calleeTimeOut(query.getCalleeTimeOut()).routeType(3).build();

        callInfo.addUniqueIdList(uniqueId);
        callInfo.setProcess(ProcessEnum.CALL_OTHER);

        //获取被叫显号
        CallDisplayQuery poolQuery = new CallDisplayQuery();
        List<CallDisplayVo> displayList = iCallDisplayService.getList(poolQuery);
        if (CollectionUtil.isEmpty(displayList)) {
            throw new CommonException("租户未配置显号");
        }
        CallDisplayVo callDisplay = RandomUtil.randomEle(displayList);
        callInfo.setCallerDisplay(query.getCallee());
        callInfo.setCalleeDisplay(callDisplay.getPhone());

        CorpInfo corpInfo = corpInfoService.getByCode(SecurityUtils.getCorpCode());
        if(Objects.nonNull(corpInfo)){
            callInfo.setCdrNotifyUrl(corpInfo.getCallBackPath());
        }
        //构建主叫通道
        ChannelInfo channelInfo = ChannelInfo.builder().callId(callId).uniqueId(uniqueId).cdrType(2).type(1)
                .agentId(sipAgent.getId()).agentNumber(sipAgent.getAgentNumber()).agentName(sipAgent.getName())
                .callTime(DateUtil.current())
                .caller(callInfo.getCallee()).called(callInfo.getCaller()).display(callInfo.getCalleeDisplay()).build();
        callInfo.setChannelInfoMap(uniqueId, channelInfo);

        iFsCallCacheService.saveCallInfo(callInfo);
        iFsCallCacheService.saveCallRel(uniqueId, callId);

        //查询主叫路由
        CallRouteVo callRoute = iFsCallCacheService.getCallRoute(callInfo.getCaller(), 2);
        if (Objects.isNull(callRoute)) {
            throw new CommonException("未配置号码路由");
        }
        if (CollectionUtil.isEmpty(callRoute.getGatewayList())) {
            throw new CommonException("号码路由未关联网关信息");
        }

        fsClient.makeCall(callId, callInfo.getCaller(), callInfo.getCallerDisplay(), uniqueId, query.getCallerTimeOut(), callRoute);
        return callId;
    }

    @Override
    public CallRecordVo getCallInfo(Long callId) {
        CallRecordVo callRecordVo = new CallRecordVo();
        CallRecord callRecord = iCallRecordService.getById(callId);
        if(Objects.isNull(callRecord)){
            throw new CommonException("呼叫记录不存在");
        }
        BeanUtils.copyProperties(callRecord, callRecordVo);
        return callRecordVo;
    }

    @Override
    public List<CallRecordVo> getCallPageList(CallRecordQuery query) {
        return iCallRecordService.getCallPageList(query);
    }



}
