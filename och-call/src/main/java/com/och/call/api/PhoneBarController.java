package com.och.call.api;

import lombok.RequiredArgsConstructor;

import com.och.call.service.impl.PhoneBarApplicationServiceImpl;
import com.och.call.domain.CallRecord;
import com.och.call.domain.ServiceRecord;
import com.och.common.api.ApiResponse;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电话条控制器。
 * <p>
 * 只负责 HTTP 参数接收、权限入口和统一响应封装；接听、挂断、保持、转接等业务编排
 * 下沉到 {@link PhoneBarApplicationServiceImpl}，避免 Controller 直接操作 Mapper、状态机和 ESL 命令。
 * </p>
 *
 * @author danmo
 */
@Tag(name = "PhoneBar", description = "电话条操作")
@RestController
@RequestMapping("/api/agent/phone-bar")
@RequiredArgsConstructor
public class PhoneBarController {

    private final PhoneBarApplicationServiceImpl phoneBarService;

    @Operation(summary = "接听")
    @PostMapping("/answer")
    @Audited(action = "CALL_ANSWER", targetType = "CALL")
    public ApiResponse<Void> answer(@RequestParam String callId) {
        phoneBarService.answer(callId);
        return ApiResponse.success();
    }

    @Operation(summary = "挂断")
    @PostMapping("/hangup")
    @Audited(action = "CALL_HANGUP", targetType = "CALL")
    public ApiResponse<Void> hangup(@RequestParam String callId, @RequestParam(required = false) String cause) {
        phoneBarService.hangup(callId, cause);
        return ApiResponse.success();
    }

    @Operation(summary = "保持")
    @PostMapping("/hold")
    @Audited(action = "CALL_HOLD", targetType = "CALL")
    public ApiResponse<Void> hold(@RequestParam String callId) {
        phoneBarService.hold(callId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消保持")
    @PostMapping("/unhold")
    @Audited(action = "CALL_UNHOLD", targetType = "CALL")
    public ApiResponse<Void> unhold(@RequestParam String callId) {
        phoneBarService.unhold(callId);
        return ApiResponse.success();
    }

    @Operation(summary = "盲转")
    @PostMapping("/blind-transfer")
    @Audited(action = "CALL_BLIND_TRANSFER", targetType = "CALL")
    public ApiResponse<Void> blindTransfer(@RequestParam String callId, @RequestParam String targetNumber) {
        phoneBarService.blindTransfer(callId, targetNumber);
        return ApiResponse.success();
    }

    @Operation(summary = "咨询转")
    @PostMapping("/consult-transfer")
    @Audited(action = "CALL_CONSULT_TRANSFER", targetType = "CALL")
    public ApiResponse<Void> consultTransfer(@RequestParam String callId, @RequestParam String targetNumber) {
        phoneBarService.consultTransfer(callId, targetNumber);
        return ApiResponse.success();
    }

    @Operation(summary = "提交后处理")
    @PostMapping("/after-call-work")
    public ApiResponse<ServiceRecord> submitAfterCallWork(@RequestBody ServiceRecord record) {
        return ApiResponse.success(phoneBarService.submitAfterCallWork(record));
    }

    @Operation(summary = "获取通话记录")
    @GetMapping("/records")
    public ApiResponse<List<CallRecord>> getCallRecords(@RequestParam(required = false) Long agentId) {
        return ApiResponse.success(phoneBarService.listCallRecords(agentId));
    }

    @Operation(summary = "获取服务记录")
    @GetMapping("/service-records")
    public ApiResponse<List<ServiceRecord>> getServiceRecords(@RequestParam(required = false) String callId) {
        return ApiResponse.success(phoneBarService.listServiceRecords(callId));
    }
}
