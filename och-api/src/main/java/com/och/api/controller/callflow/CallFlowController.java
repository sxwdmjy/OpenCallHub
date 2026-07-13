package com.och.api.controller.callflow;

import com.och.api.service.ICallFlowService;
import com.och.call.domain.CallSession;
import com.och.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 呼叫流程编排控制器
 * <p>
 * 呼入全链路的聚合入口：启动IVR → DTMF交互 → ACD分配 → 座席通知。
 * 串联 och-call / och-ivr / och-acd / och-websocket 四大模块。
 * </p>
 *
 * @author danmo
 */
@Tag(name = "CallFlow", description = "呼叫流程编排")
@RestController
@RequestMapping("/api/call-flow")
@RequiredArgsConstructor
public class CallFlowController {

    private final ICallFlowService callFlowService;

    @Operation(summary = "启动呼入流程（会话→IVR→ACD）")
    @PostMapping("/inbound/start")
    public ApiResponse<CallSession> startInbound(
            @RequestParam String callId,
            @RequestParam String calledNumber,
            @RequestParam String callerNumber) {
        return ApiResponse.success(callFlowService.startInboundFlow(callId, calledNumber, callerNumber));
    }

    @Operation(summary = "手动触发DTMF事件")
    @PostMapping("/dtmf")
    public ApiResponse<Void> handleDtmf(
            @RequestParam String callId,
            @RequestParam String digit) {
        callFlowService.handleDtmf(callId, digit);
        return ApiResponse.success();
    }

    @Operation(summary = "查询当前呼叫流程状态")
    @GetMapping("/status/{callId}")
    public ApiResponse<ICallFlowService.FlowStatus> getStatus(@PathVariable String callId) {
        return ApiResponse.success(callFlowService.getFlowStatus(callId));
    }
}