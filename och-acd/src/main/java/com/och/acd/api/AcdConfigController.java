package com.och.acd.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.acd.service.impl.AcdOverflowServiceImpl;
import com.och.acd.service.impl.QueueDispatchServiceImpl;
import com.och.acd.application.strategy.DispatchStrategyFactory;
import com.och.acd.domain.*;
import com.och.acd.service.ISkillGroupService;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ACD排队分配管理控制器
 * <p>
 * 提供技能组 CRUD、呼叫入队、座席状态管理、队列统计、溢出处理和坐席释放等 REST API。
 * 数据操作委托给 {@link ISkillGroupService}，业务编排委托给 {@link QueueDispatchServiceImpl}，
 * 溢出处理委托给 {@link AcdOverflowServiceImpl}。
 * </p>
 */
@Tag(name = "ACD", description = "排队分配管理")
@RestController
@RequestMapping("/api/acd")
@RequiredArgsConstructor
public class AcdConfigController {

    private final ISkillGroupService skillGroupService;
    private final QueueDispatchServiceImpl dispatchService;
    private final AcdOverflowServiceImpl overflowService;
    private final DispatchStrategyFactory strategyFactory;

    // ==================== 技能组管理 ====================

    @Operation(summary = "技能组列表")
    @GetMapping("/skill-groups")
    public ApiResponse<PageResponse<SkillGroup>> listSkillGroups(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<SkillGroup> page = skillGroupService.pageList(pageNum, pageSize);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @PostMapping("/skill-groups")
    @Audited(action = "CREATE", targetType = "SKILL_GROUP")
    public ApiResponse<SkillGroup> createSkillGroup(@RequestBody SkillGroup group) {
        return ApiResponse.success(skillGroupService.create(group));
    }

    @PutMapping("/skill-groups/{id}")
    @Audited(action = "UPDATE", targetType = "SKILL_GROUP")
    public ApiResponse<SkillGroup> updateSkillGroup(@PathVariable Long id, @RequestBody SkillGroup group) {
        return ApiResponse.success(skillGroupService.update(id, group));
    }

    @DeleteMapping("/skill-groups/{id}")
    @Audited(action = "DELETE", targetType = "SKILL_GROUP")
    public ApiResponse<Void> deleteSkillGroup(@PathVariable Long id) {
        skillGroupService.delete(id);
        return ApiResponse.success();
    }

    // ==================== 排队与分配 ====================

    @Operation(summary = "呼叫入队")
    @PostMapping("/enqueue")
    public ApiResponse<Map<String, Object>> enqueue(
            @RequestParam Long skillGroupId,
            @RequestParam String callId,
            @RequestParam Long callSessionId,
            @RequestParam String callerNumber) {
        // 检查队列是否已满
        if (overflowService.isQueueFull(skillGroupId)) {
            return ApiResponse.success(Map.of("state", "OVERFLOW", "reason", "QUEUE_FULL"));
        }

        AcdQueueSession session = dispatchService.enqueue(skillGroupId, callId, callSessionId, callerNumber);
        return ApiResponse.success(Map.of("state", session.getState(), "position", session.getPosition()));
    }

    @Operation(summary = "获取队列统计（含SLA指标）")
    @GetMapping("/queues/{skillGroupId}/stats")
    public ApiResponse<QueueDispatchServiceImpl.QueueStats> getQueueStats(@PathVariable Long skillGroupId) {
        return ApiResponse.success(dispatchService.getQueueStats(skillGroupId));
    }

    @Operation(summary = "处理超时溢出")
    @PostMapping("/queues/{skillGroupId}/overflow")
    public ApiResponse<Map<String, Object>> processOverflow(@PathVariable Long skillGroupId) {
        int count = overflowService.processTimeoutOverflow(skillGroupId);
        return ApiResponse.success(Map.of("overflowCount", count));
    }

    @Operation(summary = "获取可用分配策略列表")
    @GetMapping("/strategies")
    public ApiResponse<Map<String, String>> getStrategies() {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put("IDLE_FIRST", "空闲优先");
        map.put("ROUND_ROBIN", "轮询分配");
        map.put("LEAST_CALLS", "最少通话数");
        map.put("SKILL_PRIORITY", "技能等级优先");
        return ApiResponse.success(map);
    }

    // ==================== 座席管理 ====================

    @Operation(summary = "更新坐席状态")
    @PutMapping("/agents/{agentId}/state")
    public ApiResponse<Void> updateAgentState(
            @PathVariable Long agentId,
            @RequestParam Long skillGroupId,
            @RequestParam String state) {
        dispatchService.updateAgentState(agentId, skillGroupId, AgentState.valueOf(state));
        return ApiResponse.success();
    }

    @Operation(summary = "释放坐席")
    @PostMapping("/release")
    public ApiResponse<Void> releaseAgent(@RequestParam Long callSessionId, @RequestParam String endCause) {
        dispatchService.releaseAgent(callSessionId, endCause);
        return ApiResponse.success();
    }
}