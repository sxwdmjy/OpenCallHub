package com.och.ivr.api;

import com.och.common.api.ApiResponse;
import com.och.ivr.domain.IvrFlow;
import com.och.ivr.service.IIvrFlowService;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IVR流程管理控制器
 * <p>
 * 提供 IVR 流程的完整 REST API（CRUD + 版本发布）。
 * 业务逻辑委托给 {@link IIvrFlowService}，Controller 只负责参数接收、权限校验和响应封装。
 * </p>
 */
@Slf4j
@Tag(name = "IVR", description = "IVR流程管理")
@RestController
@RequestMapping("/api/ivr/flows")
@RequiredArgsConstructor
public class IvrFlowController {

    private final IIvrFlowService flowService;

    @Operation(summary = "获取IVR流程列表")
    @GetMapping
    public ApiResponse<List<IvrFlow>> list() {
        List<IvrFlow> flows = flowService.list();
        log.debug("[IVR] 查询流程列表: 共{}条", flows.size());
        return ApiResponse.success(flows);
    }

    @Operation(summary = "获取IVR流程详情")
    @GetMapping("/{id}")
    public ApiResponse<IvrFlow> get(@PathVariable Long id) {
        IvrFlow flow = flowService.getById(id);
        if (flow == null) {
            log.warn("[IVR] 流程不存在: id={}", id);
        } else {
            log.debug("[IVR] 查询流程详情: id={}, name={}", id, flow.getFlowName());
        }
        return ApiResponse.success(flow);
    }

    @Operation(summary = "创建IVR流程")
    @PostMapping
    @Audited(action = "CREATE", targetType = "IVR_FLOW")
    public ApiResponse<IvrFlow> create(@RequestBody IvrFlow flow) {
        IvrFlow created = flowService.create(flow);
        log.info("[IVR] 创建流程成功: id={}, name={}, code={}",
                created.getId(), created.getFlowName(), created.getFlowCode());
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新IVR流程")
    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "IVR_FLOW")
    public ApiResponse<IvrFlow> update(@PathVariable Long id, @RequestBody IvrFlow flow) {
        IvrFlow updated = flowService.update(id, flow);
        log.info("[IVR] 更新流程成功: id={}, name={}", id, updated.getFlowName());
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除IVR流程")
    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "IVR_FLOW")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        flowService.delete(id);
        log.info("[IVR] 删除流程成功: id={}", id);
        return ApiResponse.success();
    }

    @Operation(summary = "发布IVR版本")
    @PostMapping("/{id}/publish")
    @Audited(action = "PUBLISH", targetType = "IVR_FLOW")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        flowService.publish(id);
        log.info("[IVR] 发布流程成功: id={}", id);
        return ApiResponse.success();
    }
}