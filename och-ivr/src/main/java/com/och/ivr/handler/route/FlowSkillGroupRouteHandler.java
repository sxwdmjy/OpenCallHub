package com.och.ivr.handler.route;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.och.common.annotation.EslRouteName;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.constant.FlowDataContext;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.HangupCauseEnum;
import com.och.common.enums.ProcessEnum;
import com.och.common.enums.RouteTypeEnum;
import com.och.common.enums.SipAgentStatusEnum;
import com.och.common.thread.ThreadFactoryImpl;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.handler.route.FsAbstractRouteHandler;
import com.och.esl.queue.CallQueue;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.properties.FlowNodeProperties;
import com.och.ivr.properties.FlowTransferNodeProperties;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.agent.SipAgentStatusVo;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.domain.vo.skill.CallSkillAgentRelVo;
import com.och.system.domain.vo.skill.CallSkillVo;
import com.och.system.service.ICallSkillService;
import com.och.system.service.IFsSipGatewayService;
import com.och.system.service.ISipAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author danmo
 * @date 2024-12-31 17:20
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class FlowSkillGroupRouteHandler implements InitializingBean, DisposableBean {

    private final IFsCallCacheService fsCallCacheService;
    private final ICallSkillService iCallSkillService;
    private final FsClient fsClient;
    private final RedisService redisService;
    private final IFsSipGatewayService iFsSipGatewayService;
    private final ISipAgentService iSipAgentService;
    protected final IFlowNoticeService iFlowNoticeService;
    private final Map<Long, FlowDataContext> channelInfoMap = new ConcurrentHashMap<>();

    private Map<Long, PriorityQueue<CallQueue>> callQueueMap = new ConcurrentHashMap<>();

    private ThreadPoolExecutor callAgentExecutor = new ThreadPoolExecutor(5, 10, 60L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("flow-agent-distribution-pool-%d"));
    /**
     * 定时线程组
     */
    private static ScheduledExecutorService fsAcdThread = new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl("flow-acd-pool-%d"));


    public void handler(FlowDataContext flowData, FlowTransferNodeProperties properties) {
        channelInfoMap.put(flowData.getCallId(), flowData);
        String skillId = properties.getRouteValue();
        log.info("转技能组 callId:{} transfer to {}", flowData.getCallId(), skillId);

        CallInfo callInfo = fsCallCacheService.getCallInfo(flowData.getCallId());

        CallSkillVo callSkill = iCallSkillService.getDetail(Long.valueOf(skillId));
        if (Objects.isNull(callSkill)) {
            log.info("转技能组获取技能组失败 callee:{}, skillId:{}", callInfo.getCallee(), skillId);
            fsClient.hangupCall(flowData.getAddress(), callInfo.getCallId(), flowData.getUniqueId());
            iFlowNoticeService.notice(2, "end", flowData);
            channelInfoMap.remove(flowData.getCallId());
            return;
        }
        callInfo.setSkillId(callSkill.getId());

        //电话经过技能组
        CallInfoDetail detail = new CallInfoDetail();
        detail.setCallId(callInfo.getCallId());
        detail.setCreateTime(DateUtil.current());
        detail.setStartTime(DateUtil.current());
        detail.setOrderNum(callInfo.getDetailList() == null ? 0 : callInfo.getDetailList().size() + 1);
        detail.setTransferType(3);
        detail.setTransferId(callInfo.getSkillId());
        callInfo.addDetailList(detail);


        List<CallSkillAgentRelVo> agentList = callSkill.getAgentList();
        if (CollectionUtil.isEmpty(agentList)) {
            log.info("转技能组未配置坐席失败 callee:{}, skillId:{}", callInfo.getCallee(), skillId);
            callInfo.setSkillHangUpReason(HangupCauseEnum.FULLBUSY.getDesc());
            fsClient.hangupCall(flowData.getAddress(), callInfo.getCallId(), flowData.getUniqueId());
            iFlowNoticeService.notice(2, "end", flowData);
            channelInfoMap.remove(flowData.getCallId());
            return;
        }
        List<String> agentIds = agentList.stream().map(CallSkillAgentRelVo::getAgentId).map(String::valueOf).toList();

        List<SipAgentStatusVo> callSipAgentStatusList = redisService.getMultiCacheMapValue(CacheConstants.AGENT_CURRENT_STATUS_KEY, agentIds);

        List<SipAgentStatusVo> freeAgentList = new LinkedList<>();

        //获取空闲状态坐席列表
        if (CollectionUtil.isNotEmpty(callSipAgentStatusList)) {
            List<SipAgentStatusVo> sipAgentStatusVoList = callSipAgentStatusList.stream()
                    .filter(item -> Objects.equals(SipAgentStatusEnum.READY.getCode(), item.getStatus()))
                    .toList();
            freeAgentList.addAll(sipAgentStatusVoList);
        }


        //无空闲坐席
        if (CollectionUtil.isEmpty(freeAgentList)) {
            Integer fullBusyType = callSkill.getFullBusyType();
            switch (fullBusyType) {
                //排队
                case 0:
                    callQueueStrategy(flowData.getAddress(), callInfo, flowData.getUniqueId(), callSkill);
                    break;
                //溢出
                case 1:
                    overFlowStrategy(flowData.getAddress(), callInfo, flowData.getUniqueId(), callSkill);
                    break;
                //挂机
                case 2:
                    callInfo.setHangupDir(3);
                    callInfo.setHangupCause(HangupCauseEnum.OVERFLOW.getCode());
                    fsClient.hangupCall(flowData.getAddress(), callInfo.getCallId(), flowData.getUniqueId());
                    fsCallCacheService.saveCallInfo(callInfo);
                    iFlowNoticeService.notice(2, "end", flowData);
                    break;
                default:
                    break;
            }
            return;
        }

        Map<Long, Integer> skillLevelMap = agentList.stream().collect(Collectors.toMap(CallSkillAgentRelVo::getAgentId, CallSkillAgentRelVo::getLevel, (key1, key2) -> key1));
        freeAgentList.forEach(item -> item.setLevel(skillLevelMap.get(item.getId())));
        //获取坐席策略
        SipAgentVo agentInfo = getAgentStrategy(callInfo, callSkill, freeAgentList);
        if (Objects.isNull(agentInfo)) {
            log.info("转技能组 获取空闲坐席失败 callee:{}, skillId:{}", callInfo.getCallee(), skillId);
            callInfo.setSkillHangUpReason(HangupCauseEnum.FULLBUSY.getDesc());
            fsClient.hangupCall(flowData.getAddress(), callInfo.getCallId(), flowData.getUniqueId());
            iFlowNoticeService.notice(2, "end", flowData);
            channelInfoMap.remove(flowData.getCallId());
            return;
        }
        transferAgentHandler(flowData.getAddress(),agentInfo, callInfo,flowData.getUniqueId());
    }

    private void callQueueStrategy(String address, CallInfo callInfo, String uniqueId, CallSkillVo skill) {
        PriorityQueue<CallQueue> callQueues = callQueueMap.get(callInfo.getSkillId());
        if (callQueues == null) {
            callQueues = new PriorityQueue<CallQueue>();
        }
        if(callQueues.size() >= skill.getQueueLength()){
            overFlowStrategy(address, callInfo, uniqueId, skill);
            return;
        }
        callInfo.setQueueStartTime(DateUtil.current());
        if (callInfo.getFirstQueueTime() == null) {
            callInfo.setFirstQueueTime(callInfo.getQueueStartTime());
        }
        callQueues.add(CallQueue.builder().callId(callInfo.getCallId())
                .skillId(callInfo.getSkillId())
                .address(address)
                .type(1)
                .startTime(callInfo.getQueueStartTime())
                .uniqueId(uniqueId).build());
        callQueueMap.put(callInfo.getSkillId(), callQueues);
        fsCallCacheService.saveCallInfo(callInfo);
    }

    private void overFlowStrategy(String address, CallInfo callInfo, String uniqueId, CallSkillVo skill) {
        callInfo.setOverflowCount(callInfo.getOverflowCount() == null ? 0 : callInfo.getOverflowCount() + 1);
        if (skill.getOverflowType() == 0) {
            callInfo.setHangupDir(3);
            callInfo.setHangupCause(HangupCauseEnum.FULLBUSY.getCode());
            //坐席繁忙音
            fsClient.playFile(address,uniqueId,"agentbusy.wav");
            fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
        } else if (skill.getOverflowType() == 1) {
            //todo 转IVR
        }
        fsCallCacheService.saveCallInfo(callInfo);
    }


    private SipAgentVo getAgentStrategy(CallInfo callInfo, CallSkillVo skill, List<SipAgentStatusVo> freeAgentList) {
        switch (skill.getStrategyType()) {
            //0-随机
            case 0:
                return RandomUtil.randomEle(freeAgentList);
            //1-轮询
            case 1:
                String pollingKey = StringUtils.format(CacheConstants.CALL_SKILL_POLLING_KEY, skill.getId());
                int pollingNum = redisService.keyIsExists(pollingKey) ? redisService.getCacheObject(pollingKey) : 0;
                Integer nextPollingNum = findNextPollingNum(freeAgentList, pollingNum);
                redisService.setCacheObject(StringUtils.format(CacheConstants.CALL_SKILL_POLLING_KEY, skill.getId()), nextPollingNum);
                return freeAgentList.get(nextPollingNum);
            //2-最长空闲时间
            case 2:
                return freeAgentList.stream().max(Comparator.comparing(item -> DateUtil.current() - item.getStatusTime())).orElse(null);
            //3-当天最少应答次数
            case 3:
                return freeAgentList.stream().min(Comparator.comparing(SipAgentStatusVo::getReceptionNum)).orElse(null);
            //4-最长话后时长
            case 4:
                return freeAgentList.stream().min(Comparator.comparing(item -> DateUtil.current() - item.getCallEndTime())).orElse(null);
            default:
                return null;
        }
    }




    //acd排队策略
    private void fsAcd() {
        try {
            Set<Long> skillIds = callQueueMap.keySet();
            List<CallQueue> queueList = callQueueMap.values().stream().flatMap(Collection::stream).toList();
            if(CollectionUtil.isEmpty(skillIds) || CollectionUtil.isEmpty(queueList)){
                return;
            }
            CallSkillQuery skillQuery = new CallSkillQuery();
            skillQuery.setIds(new ArrayList<>(skillIds));
            List<CallSkillVo> callSkillList = iCallSkillService.getListByIds(skillQuery);
            if(CollectionUtil.isEmpty(callSkillList)){
                return;
            }
            long current = DateUtil.current();
            callSkillList.sort(Comparator.comparing(CallSkillVo::getPriority));
            for (CallSkillVo skill : callSkillList) {
                PriorityQueue<CallQueue> callQueues = callQueueMap.get(skill.getId());
                Iterator<CallQueue> iterator = callQueues.iterator();
                while (iterator.hasNext()){
                    CallQueue callQueue = iterator.next();
                    //正常排队超时
                    if (current/1000 - callQueue.getStartTime()/1000 >= skill.getTimeOut()) {
                        callAgentExecutor.execute(() -> {
                            queueTimeout(callQueue);
                        });
                        iterator.remove();
                        continue;
                    }
                    CallInfo callInfo = fsCallCacheService.getCallInfo(callQueue.getCallId());
                    if (callInfo == null) {
                        continue;
                    }

                    List<CallSkillAgentRelVo> agentList = skill.getAgentList();
                    List<String> agentIds = agentList.stream().map(CallSkillAgentRelVo::getAgentId).map(String::valueOf).toList();

                    List<SipAgentStatusVo> callSipAgentStatusList = redisService.getMultiCacheMapValue(CacheConstants.AGENT_CURRENT_STATUS_KEY, agentIds);
                    List<SipAgentStatusVo> freeAgentList = new LinkedList<>();
                    //获取空闲状态坐席列表
                    if (CollectionUtil.isNotEmpty(callSipAgentStatusList)) {
                        List<SipAgentStatusVo> sipAgentStatusVoList = callSipAgentStatusList.stream()
                                .filter(item -> Objects.equals(SipAgentStatusEnum.READY.getCode(), item.getStatus()))
                                .toList();
                        freeAgentList.addAll(sipAgentStatusVoList);
                    }

                    if(CollectionUtil.isEmpty(freeAgentList)){
                        if (callQueue.getPlayFlag() == null || !callQueue.getPlayFlag()) {
                            fsClient.playFile(callQueue.getAddress(), callQueue.getUniqueId(), "queue.wav");
                            callQueue.setPlayFlag(true);
                        }
                        continue;
                    }
                    Map<Long, Integer> skillLevelMap = agentList.stream().collect(Collectors.toMap(CallSkillAgentRelVo::getAgentId, CallSkillAgentRelVo::getLevel, (key1, key2) -> key1));
                    freeAgentList.forEach(item -> item.setLevel(skillLevelMap.get(item.getId())));

                    SipAgentVo agentInfo = getAgentStrategy(callInfo, skill, freeAgentList);
                    if (Objects.isNull(agentInfo)) {
                        log.info("转技能组 队列获取空闲坐席失败 callee:{}, skillId:{}", callInfo.getCallee(), skill.getId());
                        if (!callQueue.getPlayFlag()) {
                            fsClient.playFile(callQueue.getAddress(), callQueue.getUniqueId(), "queue.wav");
                            callQueue.setPlayFlag(true);
                        }
                        continue;
                    }
                    //停止放音
                    callAgentExecutor.execute(() -> {
                        if (callQueue.getPlayFlag()) {
                            fsClient.playBreak(callQueue.getAddress(), callQueue.getUniqueId());
                        }
                        callInfo.setQueueEndTime(current);
                        //转坐席音
                        fsClient.playFile(callQueue.getAddress(),callQueue.getUniqueId(),"agentbusy.wav");
                        transferAgentHandler(callQueue.getAddress(),agentInfo, callInfo, callQueue.getUniqueId());
                        iterator.remove();
                    });
                }
                //callQueueMap.remove(skill.getId());
            }
        } catch (Exception e) {
            log.error("排队异常 error:{}",e.getMessage(),e);
        }
    }

    private void transferAgentHandler(String address, SipAgentVo agentInfo, CallInfo callInfo, String uniqueId) {
        FlowDataContext flowData = channelInfoMap.get(callInfo.getCallId());
        String agentNumber = agentInfo.getAgentNumber();
        String otherUniqueId = RandomUtil.randomNumbers(32);

        if (StringUtils.isEmpty(agentNumber)) {
            log.error("transferAgentHandler agent:{} 坐席未绑定SIP号码, callId:{}", agentInfo.getId(), callInfo.getCallId());
            callInfo.setSkillHangUpReason(HangupCauseEnum.AGENT_NO_BAND_SIP.getDesc());
            fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
            iFlowNoticeService.notice(2, "end", flowData);
            channelInfoMap.remove(callInfo.getCallId());
            return;
        }
        CallRouteVo callRoute = fsCallCacheService.getCallRoute(callInfo.getCallee(), 1);
        if(Objects.isNull(callRoute)){
            log.info("transferAgentHandler 未配置号码路由 callee:{}",agentNumber);
            callInfo.setSkillHangUpReason(HangupCauseEnum.NOT_ROUTE.getDesc());
            fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
            iFlowNoticeService.notice(2, "end", flowData);
            channelInfoMap.remove(callInfo.getCallId());
            return;
        }
        FsSipGatewayQuery query = new FsSipGatewayQuery();
        query.setGatewayType(0);
        List<FsSipGateway> gatewayList = iFsSipGatewayService.getList(query);
        if(CollectionUtil.isEmpty(gatewayList)){
            log.info("transferAgentHandler 号码路由未关联网关信息 callee:{}",agentNumber);
            callInfo.setSkillHangUpReason(HangupCauseEnum.ROUTE_NOT_GATEWAY.getDesc());
            fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
            iFlowNoticeService.notice(2, "end", flowData);
            channelInfoMap.remove(callInfo.getCallId());
            return;
        }
        callInfo.setAgentId(agentInfo.getId());
        callInfo.setAgentNumber(agentInfo.getAgentNumber());
        callInfo.setAgentName(agentInfo.getName());
        callInfo.setCallee(agentInfo.getAgentNumber());
        //构建主叫通道
        ChannelInfo otherChannelInfo = ChannelInfo.builder().callId(callInfo.getCallId()).uniqueId(otherUniqueId).cdrType(2).type(2)
                .agentId(agentInfo.getId()).agentNumber(agentInfo.getAgentNumber()).agentName(agentInfo.getName())
                .callTime(DateUtil.current()).otherUniqueId(uniqueId)
                .called(agentNumber).caller(callInfo.getCaller()).display(callInfo.getCallerDisplay()).build();
        callInfo.setChannelInfoMap(otherUniqueId,otherChannelInfo);
        callInfo.setProcess(ProcessEnum.CALL_BRIDGE);
        fsCallCacheService.saveCallInfo(callInfo);
        fsCallCacheService.saveCallRel(otherUniqueId,callInfo.getCallId());

        //设置坐席通话中
        iSipAgentService.updateStatus(agentInfo.getId(), SipAgentStatusEnum.TALKING.getCode());

        fsClient.makeCall(address,callInfo.getCallId(), callInfo.getCallee(),callInfo.getCallerDisplay(),otherUniqueId,callInfo.getCalleeTimeOut(), gatewayList.get(0));

        Boolean isAgent = redisService.getCacheMapHasKey(CacheConstants.AGENT_CURRENT_STATUS_KEY, String.valueOf(callInfo.getAgentId()));
        if(isAgent){
            SipAgentStatusVo agentStatusVo = redisService.getCacheMapValue(CacheConstants.AGENT_CURRENT_STATUS_KEY, String.valueOf(callInfo.getAgentId()));
            agentStatusVo.setReceptionNum(agentStatusVo.getReceptionNum() + 1);
            redisService.setCacheMapValue(CacheConstants.AGENT_CURRENT_STATUS_KEY,String.valueOf(agentStatusVo.getId()),agentStatusVo);
        }
        iFlowNoticeService.notice(2, "next", flowData);
        channelInfoMap.remove(callInfo.getCallId());
    }

    /**
     * 超时挂机
     * @param callQueue
     */
    private void queueTimeout(CallQueue callQueue) {
        Long callId = callQueue.getCallId();
        fsClient.playBreak(callQueue.getAddress(), callQueue.getUniqueId());
        Long current = DateUtil.current();
        CallInfo callInfo = fsCallCacheService.getCallInfo(callQueue.getCallId());
        if (callInfo == null) {
            return;
        }
        callInfo.setQueueEndTime(current);
        log.info("排队超时 callId:{} queueTimeout:{}", callQueue.getCallId(), callQueue.getStartTime());
        //排队超时挂机
        callInfo.setHangupDir(3);
        callInfo.setHangupCause(HangupCauseEnum.QUEUE_TIME_OUT.getCode());
        callInfo.setQueueEndTime(current);
        if (!CollectionUtils.isEmpty(callInfo.getDetailList())) {
            CallInfoDetail callDetail = callInfo.getDetailList().get(callInfo.getDetailList().size() - 1);
            if (callDetail != null) {
                callDetail.setEndTime(current);
            }
        }
        callInfo.setSkillHangUpReason(HangupCauseEnum.QUEUE_TIME_OUT.getDesc());

        fsClient.hangupCall(callQueue.getAddress(), callQueue.getCallId(), callQueue.getUniqueId());
        fsCallCacheService.saveCallInfo(callInfo);
        iFlowNoticeService.notice(2, "end", channelInfoMap.get(callId));
        channelInfoMap.remove(callId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("开启呼入队列扫描");
        fsAcdThread.scheduleAtFixedRate(this::fsAcd, 5, 2, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        log.info("关闭呼入队列扫描");
        fsAcdThread.shutdown();
    }
    /**
     * 二分查询轮训级别
     *
     * @param freeAgentList
     * @param pollingNum
     * @return
     */
   private Integer findNextPollingNum(List<SipAgentStatusVo> freeAgentList, int pollingNum) {
        freeAgentList.sort(Comparator.comparing(SipAgentStatusVo::getLevel));
        int left = 0;
        int right = freeAgentList.size() - 1;
        if(pollingNum >= freeAgentList.get(right).getLevel()){
            return freeAgentList.get(left).getLevel();
        }
        while (left < right) {
            //防止整数溢出
            int mid = (right - left) / 2 + left;
            //如果当前元素比目标元素小或者相同 则去他右边找
            if (freeAgentList.get(mid).getLevel() <= pollingNum) {
                left = mid + 1;
            } else {
                //如果当前元素比目标元素大 则去他左边找 此时注意 应该包括其当前边界
                right = mid;
            }
        }
        return freeAgentList.get(left).getLevel();
    }
}
