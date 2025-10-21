package com.och.calltask.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.vo.CallTaskVo;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
    public void executePredictiveCall(CallTaskAssignment assignment, CallTaskVo callTask) {
        log.info("【预测式外呼】开始执行呼叫，分配ID: {}", assignment.getId());

        try {
            // 2. 获取坐席信息
            SipAgentVo agent = sipAgentService.getDetail(assignment.getAgentId());
            if (agent == null) {
                log.error("【预测式外呼异常】坐席信息不存在，坐席ID: {}", assignment.getAgentId());
                throw new CommonException("坐席信息不存在");
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
            callInfo.setRouteType(7); // PREDICTIVE_DIALING

            // 7. 保存呼叫信息到缓存
            fsCallCacheService.saveCallInfo(callInfo);
            fsCallCacheService.saveCallRel(uniqueId, callId);

            // 8. 异步执行拨号
            CompletableFuture.runAsync(() -> {
                try {
                    executeDialing(callId, assignment, agent, uniqueId, callTask);
                } catch (Exception e) {
                    log.error("【预测式外呼异常】呼叫ID:{} 拨号失败", callId, e);
                    // 更新分配状态为失败
                    updateAssignmentStatus(assignment, 2); // 2=失败
                }
            });

            log.info("【预测式外呼】呼叫已发起，呼叫ID: {} 客户: {} 坐席: {}",
                    callId, assignment.getPhone(), agent.getAgentNumber());

        } catch (Exception e) {
            log.error("【预测式外呼异常】分配ID:{} 执行失败", assignment.getId(), e);
            // 更新分配状态为失败
            updateAssignmentStatus(assignment, 2); // 2=失败
            throw new CommonException(assignment.getId() + "执行异常: " + e.getMessage());
        }
    }

    @Override
    public void executeBatchPredictiveCalls(List<CallTaskAssignment> assignments, CallTaskVo callTask) {
        log.info("【批量预测式外呼】开始执行批量呼叫，数量: {}", assignments.size());

        int successCount = 0;
        int failureCount = 0;

        for (CallTaskAssignment assignment : assignments) {
            try {
                executePredictiveCall(assignment, callTask);
                successCount++;
                // 添加间隔，避免并发过高
                Thread.sleep(100);

            } catch (Exception e) {
                log.error("【批量外呼异常】分配ID:{} 执行失败", assignment.getId(), e);
                failureCount++;
            }
        }

        log.info("【批量预测式外呼】批量呼叫完成，成功: {} 失败: {}", successCount, failureCount);
    }

    /**
     * 更新分配状态
     */
    private void updateAssignmentStatus(CallTaskAssignment assignment, Integer status) {
        try {
            assignment.setCallStatus(status);
            //callTaskAssignmentService.updateById(assignment);
        } catch (Exception e) {
            log.warn("【状态更新异常】分配ID:{} 状态更新失败", assignment.getId(), e);
        }
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
            
            // 更新分配状态为已拨打
            contact.setCallStatus(1); // 1=已拨打
            contact.setAttemptCount(contact.getAttemptCount() + 1);
            //callTaskAssignmentService.updateById(contact);

        } catch (Exception e) {
            log.error("【拨号执行异常】呼叫ID:{} 拨号失败", callId, e);
            // 更新分配状态为失败
            contact.setCallStatus(2); // 2=失败
            //callTaskAssignmentService.updateById(contact);
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
     * 获取外显号码
     */
    private String getCallerDisplay(Long poolId) {
        if (poolId == null) {
            log.warn("【外显号码】未配置号码池");
            return "";
        }
        
        CallDisplayPoolVo poolDetail = iCallDisplayPoolService.getPoolDetail(poolId);
        if (Objects.isNull(poolDetail)) {
            log.warn("【外显号码】号码池不存在，ID: {}", poolId);
            return "";
        }
        
        if (CollectionUtils.isEmpty(poolDetail.getPhoneList())) {
            log.warn("【外显号码】号码池为空，ID: {}", poolId);
            return "";
        }
        
        Integer type = poolDetail.getType();
        switch (type) {
            //随机
            case 1 -> {
                CallDisplaySimpleVo phone = RandomUtil.randomEle(poolDetail.getPhoneList());
                return phone != null ? phone.getDisplayNumber() : "";
            }
            //轮询
            case 2 -> {
                Integer pollingNum = redisService.keyIsExists(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId) ? 
                    redisService.getCacheObject(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId) : 0;
                Integer nextPollingNum = findNextPollingNum(poolDetail.getPhoneList(), pollingNum);
                redisService.setCacheObject(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId, nextPollingNum);
                CallDisplaySimpleVo phone = poolDetail.getPhoneList().get(nextPollingNum);
                return phone != null ? phone.getDisplayNumber() : "";
            }
            default -> {
                // 默认使用第一个号码
                CallDisplaySimpleVo phone = poolDetail.getPhoneList().get(0);
                return phone != null ? phone.getDisplayNumber() : "";
            }
        }
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
        if (StringUtils.isBlank(phone)) {
            throw new CommonException("外显号码为空");
        }
        
        CallRouteVo callRoute = lfsCallCacheService.getCallRoute(phone, 2);
        if (Objects.isNull(callRoute)) {
            throw new CommonException("未配置号码路由: " + phone);
        }

        FsSipGateway sipGateway = iFsSipGatewayService.getDetail(Long.valueOf(callRoute.getRouteValue()));
        if (Objects.isNull(sipGateway)) {
            throw new CommonException("未配置SIP网关: " + callRoute.getRouteValue());
        }
        return sipGateway;
    }
}
