package com.och.calltask.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.calltask.domain.vo.PredictiveDialingCallResult;
import com.och.calltask.service.IPredictiveDialingService;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.DirectionEnum;
import com.och.common.enums.ProcessEnum;
import com.och.common.exception.CommonException;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFsCallCacheService;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.domain.vo.display.CallDisplayPoolVo;
import com.och.system.domain.vo.display.CallDisplaySimpleVo;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.service.ICallDisplayPoolService;
import com.och.system.service.IFsSipGatewayService;
import com.och.system.service.ISipAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 预测式外呼服务实现
 *
 * @author danmo
 * @date 2025/01/15
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PredictiveDialingServiceImpl implements IPredictiveDialingService {

    private final FsClient fsClient;
    private final IFsCallCacheService fsCallCacheService;
    private final ISipAgentService sipAgentService;
    private final IFsCallCacheService lfsCallCacheService;
    private final IFsSipGatewayService iFsSipGatewayService;
    private final ICallDisplayPoolService iCallDisplayPoolService;
    private final RedisService redisService;


    @Override
    public PredictiveDialingCallResult executePredictiveCall(CallTaskAssignment assignment, CallTaskVo callTask) {
        log.info("【预测式外呼】开始执行呼叫，分配ID: {}", assignment.getId());

        try {
            // 2. 获取坐席信息
            SipAgentVo agent = sipAgentService.getDetail(assignment.getAgentId());
            if (agent == null) {
                return createErrorResult(assignment.getId(), "坐席信息不存在");
            }

            // 3. 生成呼叫ID和通道ID
            Long callId = generateCallId();
            String uniqueId = generateUniqueId();

            // 4. 构建呼叫信息
            CallInfo callInfo = buildCallInfo(callId, assignment, agent, uniqueId);

            // 5. 构建通道信息
            ChannelInfo channelInfo = buildChannelInfo(callId, assignment, agent, uniqueId);
            callInfo.setChannelInfoMap(uniqueId, channelInfo);
            // 6. 设置预测式外呼专用路由类型
            // PREDICTIVE_DIALING
            callInfo.setRouteType(7);

            // 6. 保存呼叫信息到缓存
            fsCallCacheService.saveCallInfo(callInfo);
            fsCallCacheService.saveCallRel(uniqueId, callId);

            // 7. 创建呼叫结果对象
            PredictiveDialingCallResult result = createCallResult(callId, assignment, agent, uniqueId);

            // 8. 异步执行拨号
            CompletableFuture.runAsync(() -> {
                try {
                    executeDialing(callId, assignment, agent, uniqueId,callTask);
                } catch (Exception e) {
                    log.error("【预测式外呼异常】呼叫ID:{} 拨号失败", callId, e);
                }
            });

            log.info("【预测式外呼】呼叫已发起，呼叫ID: {} 客户: {} 坐席: {}",
                    callId, assignment.getPhone(), agent.getAgentNumber());

            return result;

        } catch (Exception e) {
            log.error("【预测式外呼异常】分配ID:{} 执行失败", assignment.getId(), e);
            return createErrorResult(assignment.getId(), "执行异常: " + e.getMessage());
        }
    }

    @Override
    public List<PredictiveDialingCallResult> executeBatchPredictiveCalls(List<CallTaskAssignment> assignments, CallTaskVo callTask) {
        log.info("【批量预测式外呼】开始执行批量呼叫，数量: {}", assignments.size());

        List<PredictiveDialingCallResult> results = new ArrayList<>();

        for (CallTaskAssignment assignment : assignments) {
            try {
                PredictiveDialingCallResult result = executePredictiveCall(assignment,callTask);
                results.add(result);

                // 添加间隔，避免并发过高
                Thread.sleep(100);

            } catch (Exception e) {
                log.error("【批量外呼异常】分配ID:{} 执行失败", assignment.getId(), e);
                results.add(createErrorResult(assignment.getId(), "批量执行异常: " + e.getMessage()));
            }
        }

        log.info("【批量预测式外呼】批量呼叫完成，成功: {} 失败: {}",
                results.stream().mapToInt(r -> "SUCCESS".equals(r.getStatus()) ? 1 : 0).sum(),
                results.stream().mapToInt(r -> "FAILED".equals(r.getStatus()) ? 1 : 0).sum());

        return results;
    }





    /**
     * 执行拨号
     */
    private void executeDialing(Long callId, CallTaskAssignment contact, SipAgentVo agent, String uniqueId, CallTaskVo callTask) {
        try {

            // 获取外显号码
            String callerDisplay = getCallerDisplay(callTask.getPhonePoolId());

            // 设置超时时间（秒）
            Integer timeout = 30;

            // 执行拨号 - 使用预测式外呼专用路由
            executePredictiveCall(callId, contact.getPhone(), callerDisplay, uniqueId, timeout);

        } catch (Exception e) {
            log.error("【拨号执行异常】呼叫ID:{} 拨号失败", callId, e);
        }
    }



    /**
     * 构建呼叫信息
     */
    private CallInfo buildCallInfo(Long callId, CallTaskAssignment contact, SipAgentVo agent, String uniqueId) {
        CallInfo callInfo = CallInfo.builder()
                .callId(callId)
                .callee(contact.getPhone())
                .agentId(agent.getId())
                .agentNumber(agent.getAgentNumber())
                .agentName(agent.getName())
                .direction(DirectionEnum.OUTBOUND.getType())
                .callTime(System.currentTimeMillis())
                .process(ProcessEnum.CALL_TASK)
                .build();
        callInfo.addUniqueIdList(uniqueId);
        return callInfo;
    }

    /**
     * 构建通道信息
     */
    private ChannelInfo buildChannelInfo(Long callId, CallTaskAssignment contact, SipAgentVo agent, String uniqueId) {
        return ChannelInfo.builder()
                .callId(callId)
                .uniqueId(uniqueId)
                .agentId(agent.getId())
                .agentNumber(agent.getAgentNumber())
                .agentName(agent.getName())
                .type(1) // 坐席
                .directionType(1) // 主叫
                .cdrType(2) // 外呼
                .caller(agent.getAgentNumber())
                .called(contact.getPhone())
                .callTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建呼叫结果
     */
    private PredictiveDialingCallResult createCallResult(Long callId,
                                                         CallTaskAssignment contact, SipAgentVo agent, String uniqueId) {
        PredictiveDialingCallResult result = new PredictiveDialingCallResult();
        result.setCallId(callId);
        result.setAssignmentId(contact.getId());
        result.setStatus("INITIATING");
        result.setMessage("正在初始化");
        result.setCreateTime(LocalDateTime.now());
        result.setAgentId(agent.getId());
        result.setContactId(contact.getId());
        result.setContactPhone(contact.getPhone());
        result.setCallerDisplay(agent.getAgentNumber());
        result.setUniqueId(uniqueId);
        return result;
    }

    /**
     * 创建错误结果
     */
    private PredictiveDialingCallResult createErrorResult(Long assignmentId, String errorMessage) {
        PredictiveDialingCallResult result = new PredictiveDialingCallResult();
        result.setAssignmentId(assignmentId);
        result.setStatus("FAILED");
        result.setMessage(errorMessage);
        result.setErrorMessage(errorMessage);
        result.setCreateTime(LocalDateTime.now());
        return result;
    }



    /**
     * 获取外显号码
     */
    private String getCallerDisplay(Long poolId) {
        CallDisplayPoolVo poolDetail = iCallDisplayPoolService.getPoolDetail(poolId);
        if (Objects.isNull(poolDetail)) {
            return "";
        }
        Integer type = poolDetail.getType();
        switch (type){
            //随机
            case 1 -> {
                return RandomUtil.randomEle(poolDetail.getPhoneList()).getDisplayNumber();
            }
            //轮询
            case 2 -> {
                Integer pollingNum = redisService.keyIsExists(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId) ? redisService.getCacheObject(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId) : 0;
                Integer nextPollingNum = findNextPollingNum(poolDetail.getPhoneList(), pollingNum);
                redisService.setCacheObject(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId, nextPollingNum);
                return poolDetail.getPhoneList().get(nextPollingNum).getDisplayNumber();
            }
        }
        return "";
    }

    private Integer findNextPollingNum(List<CallDisplaySimpleVo> phoneList, Integer pollingNum) {
        if (Objects.isNull(phoneList) || phoneList.isEmpty()) {
            return 0;
        }
        if (pollingNum >= phoneList.size()) {
            return 0;
        }
        return pollingNum + 1;
    }



    /**
     * 生成呼叫ID
     */
    private Long generateCallId() {
        return System.currentTimeMillis();
    }

    /**
     * 生成唯一ID
     */
    private String generateUniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 执行预测式外呼
     */
    private void executePredictiveCall(Long callId, String phone, String callerDisplay, String uniqueId, Integer timeout) {
        try {

            // 获取SIP网关信息（简化实现）
            FsSipGateway sipGateway = getSipGateway(callerDisplay);

            // 使用预测式外呼专用路由执行拨号
            fsClient.makeCall(callId, phone, callerDisplay, uniqueId, timeout, sipGateway);

            log.info("【预测式外呼执行】呼叫ID:{} 已发起外呼到:{}", callId, phone);

        } catch (Exception e) {
            log.error("【预测式外呼执行异常】呼叫ID:{} 外呼失败", callId, e);
            throw e;
        }
    }


    /**
     * 获取SIP网关信息
     */
    private FsSipGateway getSipGateway(String phone) {
        CallRouteVo callRoute = lfsCallCacheService.getCallRoute(phone, 2);
        if (Objects.isNull(callRoute)) {
            throw new CommonException("未配置号码路由");
        }

        FsSipGateway sipGateway = iFsSipGatewayService.getDetail(Long.valueOf(callRoute.getRouteValue()));
        if(Objects.isNull(sipGateway)){
            throw new CommonException("未配置SIP网关");
        }
        return sipGateway;
    }
}
