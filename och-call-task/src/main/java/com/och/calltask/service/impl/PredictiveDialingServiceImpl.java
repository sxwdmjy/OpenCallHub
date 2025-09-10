package com.och.calltask.service.impl;

import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.vo.CallTaskContactVo;
import com.och.calltask.domain.vo.PredictiveDialingCallResult;
import com.och.calltask.service.ICallTaskService;
import com.och.calltask.service.IPredictiveDialingService;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.DirectionEnum;
import com.och.common.enums.ProcessEnum;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFsCallCacheService;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.service.ISipAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    private final ICallTaskService callTaskService;

    // 存储呼叫结果
    private final ConcurrentHashMap<Long, PredictiveDialingCallResult> callResults = new ConcurrentHashMap<>();
    
    // 存储呼叫状态
    private final ConcurrentHashMap<Long, String> callStatuses = new ConcurrentHashMap<>();

    @Override
    public PredictiveDialingCallResult executePredictiveCall(CallTaskAssignment assignment) {
        log.info("【预测式外呼】开始执行呼叫，分配ID: {}", assignment.getId());
        
        try {
            // 1. 获取客户信息
            CallTaskContactVo contact = getContactInfo(assignment.getId());
            if (contact == null) {
                return createErrorResult(assignment.getId(), "客户信息不存在");
            }

            // 2. 获取坐席信息
            SipAgentVo agent = sipAgentService.getDetail(assignment.getAgentId());
            if (agent == null) {
                return createErrorResult(assignment.getId(), "坐席信息不存在");
            }

            // 3. 生成呼叫ID和通道ID
            Long callId = generateCallId();
            String uniqueId = generateUniqueId();

        // 4. 构建呼叫信息
        CallInfo callInfo = buildCallInfo(callId, contact, agent, uniqueId);
        
        // 5. 构建通道信息
        ChannelInfo channelInfo = buildChannelInfo(callId, contact, agent, uniqueId);
        
        // 6. 设置预测式外呼专用路由类型
        callInfo.setRouteType(7); // PREDICTIVE_DIALING

            // 6. 保存呼叫信息到缓存
            fsCallCacheService.saveCallInfo(callInfo);
            fsCallCacheService.saveCallRel(uniqueId, callId);

            // 7. 创建呼叫结果对象
            PredictiveDialingCallResult result = createCallResult(callId, assignment, contact, agent, uniqueId);
            callResults.put(callId, result);
            callStatuses.put(callId, "INITIATING");

            // 8. 异步执行拨号
            CompletableFuture.runAsync(() -> {
                try {
                    executeDialing(callId, contact, agent, uniqueId, result);
                } catch (Exception e) {
                    log.error("【预测式外呼异常】呼叫ID:{} 拨号失败", callId, e);
                    updateCallResult(callId, "FAILED", "拨号异常: " + e.getMessage());
                }
            });

            log.info("【预测式外呼】呼叫已发起，呼叫ID: {} 客户: {} 坐席: {}", 
                    callId, contact.getPhone(), agent.getAgentNumber());

            return result;

        } catch (Exception e) {
            log.error("【预测式外呼异常】分配ID:{} 执行失败", assignment.getId(), e);
            return createErrorResult(assignment.getId(), "执行异常: " + e.getMessage());
        }
    }

    @Override
    public List<PredictiveDialingCallResult> executeBatchPredictiveCalls(List<CallTaskAssignment> assignments) {
        log.info("【批量预测式外呼】开始执行批量呼叫，数量: {}", assignments.size());
        
        List<PredictiveDialingCallResult> results = new ArrayList<>();
        
        for (CallTaskAssignment assignment : assignments) {
            try {
                PredictiveDialingCallResult result = executePredictiveCall(assignment);
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

    @Override
    public boolean cancelCall(Long callId) {
        log.info("【取消外呼】开始取消呼叫，呼叫ID: {}", callId);
        
        try {
            // 更新状态
            callStatuses.put(callId, "CANCELLING");
            
            // 从缓存获取呼叫信息
            CallInfo callInfo = fsCallCacheService.getCallInfo(callId);
            if (callInfo != null && callInfo.getUniqueIdList() != null) {
                // 挂断所有通道
                for (String uniqueId : callInfo.getUniqueIdList()) {
                    fsClient.hangupCall(callId, uniqueId);
                }
            }
            
            // 更新结果
            updateCallResult(callId, "CANCELLED", "呼叫已取消");
            
            log.info("【取消外呼】呼叫已取消，呼叫ID: {}", callId);
            return true;
            
        } catch (Exception e) {
            log.error("【取消外呼异常】呼叫ID:{} 取消失败", callId, e);
            return false;
        }
    }

    @Override
    public String getCallStatus(Long callId) {
        return callStatuses.getOrDefault(callId, "UNKNOWN");
    }

    @Override
    public PredictiveDialingCallResult getCallResult(Long callId) {
        return callResults.get(callId);
    }

    /**
     * 执行拨号
     */
    private void executeDialing(Long callId, CallTaskContactVo contact, SipAgentVo agent, 
                              String uniqueId, PredictiveDialingCallResult result) {
        try {
            // 更新状态为拨号中
            updateCallStatus(callId, "DIALING");
            
            // 获取外显号码（这里简化处理，实际应该从号码池获取）
            String callerDisplay = getCallerDisplay(contact.getPhone());
            
            // 设置超时时间（秒）
            Integer timeout = 30;
            
            // 执行拨号 - 使用预测式外呼专用路由
            executePredictiveCall(callId, contact.getPhone(), callerDisplay, uniqueId, timeout);
            
            // 更新结果
            result.setStatus("DIALING");
            result.setMessage("正在拨号");
            result.setStartTime(LocalDateTime.now());
            
            // 等待呼叫结果（这里简化处理，实际应该监听ESL事件）
            waitForCallResult(callId, result);
            
        } catch (Exception e) {
            log.error("【拨号执行异常】呼叫ID:{} 拨号失败", callId, e);
            updateCallResult(callId, "FAILED", "拨号执行异常: " + e.getMessage());
        }
    }

    /**
     * 等待呼叫结果
     */
    private void waitForCallResult(Long callId, PredictiveDialingCallResult result) {
        try {
            // 等待最多30秒
            for (int i = 0; i < 30; i++) {
                Thread.sleep(1000);
                
                // 检查呼叫状态
                String status = getCallStatus(callId);
                if ("ANSWERED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status)) {
                    break;
                }
            }
            
            // 最终状态检查
            String finalStatus = getCallStatus(callId);
            if (!"ANSWERED".equals(finalStatus) && !"FAILED".equals(finalStatus) && !"CANCELLED".equals(finalStatus)) {
                updateCallResult(callId, "TIMEOUT", "呼叫超时");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateCallResult(callId, "INTERRUPTED", "呼叫被中断");
        }
    }

    /**
     * 获取客户信息
     */
    private CallTaskContactVo getContactInfo(Long assignmentId) {
        try {
            // 这里应该根据assignmentId查询客户信息
            // 简化实现，返回模拟数据
            CallTaskContactVo contact = new CallTaskContactVo();
            contact.setId(assignmentId);
            contact.setPhone("13800138000");
            contact.setName("测试客户");
            return contact;
        } catch (Exception e) {
            log.error("【获取客户信息异常】分配ID:{} 获取失败", assignmentId, e);
            return null;
        }
    }

    /**
     * 构建呼叫信息
     */
    private CallInfo buildCallInfo(Long callId, CallTaskContactVo contact, SipAgentVo agent, String uniqueId) {
        return CallInfo.builder()
                .callId(callId)
                .caller(agent.getAgentNumber())
                .callee(contact.getPhone())
                .agentId(agent.getId())
                .agentNumber(agent.getAgentNumber())
                .agentName(agent.getName())
                .direction(DirectionEnum.OUTBOUND.getType())
                .callTime(System.currentTimeMillis())
                .process(ProcessEnum.CALL_ROUTE)
                .build();
    }

    /**
     * 构建通道信息
     */
    private ChannelInfo buildChannelInfo(Long callId, CallTaskContactVo contact, SipAgentVo agent, String uniqueId) {
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
    private PredictiveDialingCallResult createCallResult(Long callId, CallTaskAssignment assignment, 
                                                       CallTaskContactVo contact, SipAgentVo agent, String uniqueId) {
        PredictiveDialingCallResult result = new PredictiveDialingCallResult();
        result.setCallId(callId);
        result.setAssignmentId(assignment.getId());
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
     * 更新呼叫结果
     */
    private void updateCallResult(Long callId, String status, String message) {
        PredictiveDialingCallResult result = callResults.get(callId);
        if (result != null) {
            result.setStatus(status);
            result.setMessage(message);
            result.setUpdateTime(LocalDateTime.now());
            
            if ("ANSWERED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status)) {
                result.setEndTime(LocalDateTime.now());
                if (result.getStartTime() != null) {
                    result.setDuration(java.time.Duration.between(result.getStartTime(), result.getEndTime()).getSeconds());
                }
            }
        }
        callStatuses.put(callId, status);
    }

    /**
     * 更新呼叫状态
     */
    private void updateCallStatus(Long callId, String status) {
        callStatuses.put(callId, status);
        PredictiveDialingCallResult result = callResults.get(callId);
        if (result != null) {
            result.setStatus(status);
            result.setUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 获取外显号码
     */
    private String getCallerDisplay(String phone) {
        // 这里应该从号码池获取外显号码
        // 简化实现，返回默认号码
        return "400-123-4567";
    }

    /**
     * 获取网关地址
     */
    private String getGatewayAddress(String phone) {
        // 这里应该根据号码获取对应的网关地址
        // 简化实现，返回默认网关
        return "sip.example.com";
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
            // 获取随机FreeSWITCH地址
            String address = getRandomFsAddress();
            if (address == null) {
                throw new RuntimeException("无可用的FreeSWITCH服务器");
            }
            
            // 获取SIP网关信息（简化实现）
            FsSipGateway sipGateway = getSipGateway(phone);
            
            // 使用预测式外呼专用路由执行拨号
            fsClient.makeCall(callId, phone, callerDisplay, uniqueId, timeout, sipGateway);
            
            log.info("【预测式外呼执行】呼叫ID:{} 已发起外呼到:{}", callId, phone);
            
        } catch (Exception e) {
            log.error("【预测式外呼执行异常】呼叫ID:{} 外呼失败", callId, e);
            throw e;
        }
    }

    /**
     * 获取随机FreeSWITCH地址
     */
    private String getRandomFsAddress() {
        // 这里应该从配置或服务发现中获取FreeSWITCH地址
        // 简化实现，返回默认地址
        return "127.0.0.1:8021";
    }

    /**
     * 获取SIP网关信息
     */
    private FsSipGateway getSipGateway(String phone) {
        // 这里应该根据号码获取对应的SIP网关
        // 简化实现，返回默认网关
        FsSipGateway gateway = new FsSipGateway();
        gateway.setRealm("sip.example.com");
        gateway.setName("default");
        return gateway;
    }
}
