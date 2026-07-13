package com.och.ai.api;

import com.och.ai.service.impl.RobotFlowApplicationServiceImpl;
import com.och.ai.domain.RobotFlow;
import com.och.ai.domain.RobotFlowVersion;
import com.och.common.api.ApiResponse;
import com.och.common.base.BaseController;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 机器人流程管理
 * <p>管理 AI 机器人对话流程的创建、查询、发布和删除，发布时生成独立版本快照。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Tag(name = "RobotFlow", description = "机器人流程管理")
@RestController
@RequestMapping("/api/ai/robot-flows")
@RequiredArgsConstructor
public class RobotFlowController extends BaseController {

    private final RobotFlowApplicationServiceImpl robotFlowService;

    @Operation(summary = "获取机器人流程列表")
    @GetMapping
    public ApiResponse<List<RobotFlow>> list() {
        return success(robotFlowService.list());
    }

    @Operation(summary = "创建机器人流程")
    @PostMapping
    @Audited(action = "CREATE", targetType = "ROBOT_FLOW", description = "创建机器人流程")
    public ApiResponse<RobotFlow> create(@RequestBody RobotFlow flow) {
        return success(robotFlowService.create(flow));
    }

    @Operation(summary = "发布版本")
    @PostMapping("/{id}/publish")
    @Audited(action = "PUBLISH", targetType = "ROBOT_FLOW", description = "发布机器人流程版本")
    public ApiResponse<RobotFlowVersion> publish(@PathVariable Long id) {
        return success(robotFlowService.publish(id));
    }

    @Operation(summary = "删除机器人流程")
    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "ROBOT_FLOW", description = "删除机器人流程")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        robotFlowService.delete(id);
        return success();
    }
}
