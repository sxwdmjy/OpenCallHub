package com.och.esl.handler.route;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.och.calltask.service.IPredictiveDialingService;
import com.och.calltask.service.ICallStatusService;
import com.och.common.annotation.EslRouteName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import com.och.common.enums.RouteTypeEnum;
import com.och.system.domain.entity.FsSipGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 预测式外呼路由处理器
 * 专门处理预测式外呼的路由逻辑
 * 
 * @author danmo
 * @date 2025/01/15
 */
@RequiredArgsConstructor
@Slf4j
@EslRouteName(RouteTypeEnum.PREDICTIVE_DIALING)
@Component
public class FsPredictiveDialingRouteHandler extends FsAbstractRouteHandler {

    private final IPredictiveDialingService predictiveDialingService;
    private final ICallStatusService callStatusService;

    @Override
    public void handler(String address, CallInfo callInfo, String uniqueId, String sipGatewayId) {
        log.info("【预测式外呼路由】呼叫ID:{} 开始处理预测式外呼", callInfo.getCallId());
        
        try {
            // 1. 更新呼叫状态为路由中
            callStatusService.updateCallStatus(callInfo.getCallId(), "ROUTING", "正在路由");
            
            // 2. 获取SIP网关信息
            FsSipGateway sipGateway = iFsSipGatewayService.getDetail(Long.valueOf(sipGatewayId));
            if (sipGateway == null) {
                log.error("【预测式外呼路由异常】呼叫ID:{} SIP网关不存在:{}", callInfo.getCallId(), sipGatewayId);
                callStatusService.updateCallStatus(callInfo.getCallId(), "FAILED", "SIP网关不存在");
                fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
                return;
            }
            
            // 3. 生成被叫通道ID
            String otherUniqueId = RandomUtil.randomNumbers(32);
            
            // 4. 构建被叫通道信息
            ChannelInfo otherChannelInfo = buildCalleeChannelInfo(callInfo, otherUniqueId, uniqueId);
            callInfo.addUniqueIdList(otherUniqueId);
            callInfo.setChannelInfoMap(otherUniqueId, otherChannelInfo);
            
            // 5. 设置处理流程
            callInfo.setProcess(ProcessEnum.CALL_BRIDGE);
            
            // 6. 创建呼叫详情
            CallInfoDetail detail = createCallDetail(callInfo);
            callInfo.addDetailList(detail);
            
            // 7. 执行外呼
            executePredictiveCall(address, callInfo, otherUniqueId, sipGateway);
            
            // 8. 更新呼叫状态为拨号中
            callStatusService.updateCallStatus(callInfo.getCallId(), "DIALING", "正在拨号");
            
            // 9. 保存呼叫信息
            fsCallCacheService.saveCallInfo(callInfo);
            fsCallCacheService.saveCallRel(otherUniqueId, callInfo.getCallId());
            
            log.info("【预测式外呼路由完成】呼叫ID:{} 已发起外呼", callInfo.getCallId());
            
        } catch (Exception e) {
            log.error("【预测式外呼路由异常】呼叫ID:{} 处理失败", callInfo.getCallId(), e);
            callStatusService.updateCallStatus(callInfo.getCallId(), "FAILED", "路由处理异常: " + e.getMessage());
            fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
        }
    }

    /**
     * 构建被叫通道信息
     */
    private ChannelInfo buildCalleeChannelInfo(CallInfo callInfo, String otherUniqueId, String uniqueId) {
        return ChannelInfo.builder()
                .callId(callInfo.getCallId())
                .uniqueId(otherUniqueId)
                .cdrType(2) // 外呼
                .type(2) // 客户
                .directionType(2) // 被叫
                .callTime(DateUtil.current())
                .otherUniqueId(uniqueId)
                .called(callInfo.getCallee())
                .caller(callInfo.getCaller())
                .display(callInfo.getCallerDisplay())
                .calledLocation(callInfo.getNumberLocation())
                .build();
    }

    /**
     * 创建呼叫详情
     */
    private CallInfoDetail createCallDetail(CallInfo callInfo) {
        CallInfoDetail detail = new CallInfoDetail();
        detail.setCallId(callInfo.getCallId());
        detail.setStartTime(DateUtil.current());
        detail.setOrderNum(callInfo.getDetailList() == null ? 0 : callInfo.getDetailList().size() + 1);
        detail.setTransferType(5); // 外线
        detail.setEndTime(DateUtil.current());
        return detail;
    }

    /**
     * 执行预测式外呼
     */
    private void executePredictiveCall(String address, CallInfo callInfo, String otherUniqueId, FsSipGateway sipGateway) {
        try {
            // 使用FsClient执行外呼
            fsClient.makeCall(
                address,
                callInfo.getCallId(),
                callInfo.getCallee(),
                callInfo.getCalleeDisplay(),
                otherUniqueId,
                callInfo.getCalleeTimeOut(),
                sipGateway
            );
            
            log.info("【预测式外呼执行】呼叫ID:{} 已发起外呼到:{}", callInfo.getCallId(), callInfo.getCallee());
            
        } catch (Exception e) {
            log.error("【预测式外呼执行异常】呼叫ID:{} 外呼失败", callInfo.getCallId(), e);
            throw e;
        }
    }
}
