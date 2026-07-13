package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.service.IResourceDependencyService;
import com.och.resource.domain.ResourceDependency;
import com.och.resource.domain.ResourceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源依赖检查控制器，用于查询和管理资源间的依赖关系。
 * 提供删除前检查、依赖列表查询等接口，防止误删被引用的资源。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "ResourceDependency", description = "资源依赖检查")
@RestController
@RequestMapping("/api/resource/dependencies")
@RequiredArgsConstructor
public class ResourceDependencyController {

    private final IResourceDependencyService dependencyService;

    @Operation(summary = "检查资源是否可删除")
    @GetMapping("/check")
    public ApiResponse<DependencyCheckResult> checkDeletable(
            @RequestParam String type, @RequestParam Long id) {
        ResourceType resourceType = parseResourceType(type);
        try {
            dependencyService.checkDeletable(resourceType, id);
            return ApiResponse.success(new DependencyCheckResult(true, List.of(), "可以安全删除"));
        } catch (BizException e) {
            return ApiResponse.success(new DependencyCheckResult(false,
                    dependencyService.getDependents(resourceType, id), e.getMessage()));
        }
    }

    @Operation(summary = "获取资源依赖列表")
    @GetMapping("/{type}/{id}")
    public ApiResponse<List<ResourceDependency>> getDependencies(
            @PathVariable String type, @PathVariable Long id,
            @RequestParam(defaultValue = "dependents") String direction) {
        ResourceType resourceType = parseResourceType(type);
        List<ResourceDependency> deps;
        if ("dependencies".equals(direction)) {
            deps = dependencyService.getDependencies(resourceType, id);
        } else {
            deps = dependencyService.getDependents(resourceType, id);
        }
        return ApiResponse.success(deps);
    }

    private ResourceType parseResourceType(String type) {
        try {
            return ResourceType.valueOf(type);
        } catch (Exception e) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "不支持的资源类型: " + type);
        }
    }

    public record DependencyCheckResult(boolean deletable, List<ResourceDependency> blockingDeps, String message) {}
}
