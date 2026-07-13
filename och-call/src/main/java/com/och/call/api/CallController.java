package com.och.call.api;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.call.service.impl.CallEventIngestServiceImpl;
import com.och.call.domain.CallEvent;
import com.och.call.domain.CallSession;
import com.och.call.service.ICallEventService;
import com.och.call.service.ICallSessionService;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 呼叫会话管理控制器
 * <p>
 * 提供通话会话的查询、事件摄入等 REST API。
 * 本 Controller 只负责参数接收、权限校验和响应封装，
 * 数据查询委托给 {@link ICallSessionService} 和 {@link ICallEventService}，
 * 事件摄入委托给 {@link CallEventIngestServiceImpl}。
 * </p>
 */
@Tag(name = "Call", description = "呼叫会话管理")
@RestController("callSessionController")
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallController {

    private final ICallSessionService sessionService;
    private final ICallEventService eventService;
    private final CallEventIngestServiceImpl ingestService;

    @Operation(summary = "查询呼叫会话列表")
    @GetMapping("/sessions")
    public ApiResponse<PageResponse<CallSession>> listSessions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String keyword) {
        IPage<CallSession> page = sessionService.pageList(pageNum, pageSize, state, keyword);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @Operation(summary = "获取呼叫会话详情")
    @GetMapping("/sessions/{id}")
    public ApiResponse<CallSession> getSession(@PathVariable Long id) {
        return ApiResponse.success(sessionService.getById(id));
    }

    @Operation(summary = "获取呼叫事件列表")
    @GetMapping("/sessions/{callId}/events")
    public ApiResponse<List<CallEvent>> getEvents(@PathVariable String callId) {
        return ApiResponse.success(eventService.listByCallId(callId));
    }

    @Operation(summary = "摄入呼叫事件")
    @PostMapping("/events")
    public ApiResponse<Void> ingestEvent(@RequestBody CallEvent event) {
        ingestService.ingest(event);
        return ApiResponse.success();
    }
}