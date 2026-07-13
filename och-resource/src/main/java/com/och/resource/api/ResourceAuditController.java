package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.resource.service.IResourceAuditService;
import com.och.resource.domain.ResourceAudit;
import com.och.resource.domain.ResourceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 资源审计控制器，提供资源操作审计记录的查询功能。
 * 支持按资源类型和资源ID筛选审计日志。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "ResourceAudit", description = "资源审计")
@RestController
@RequestMapping("/api/resource/audits")
@RequiredArgsConstructor
public class ResourceAuditController {

    private final IResourceAuditService auditService;

    @Operation(summary = "查询资源审计记录")
    @GetMapping
    public ApiResponse<PageResponse<ResourceAudit>> query(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId) {
        ResourceType type = resourceType != null ? ResourceType.valueOf(resourceType) : null;
        IPage<ResourceAudit> page = auditService.queryPage(pageNum, pageSize, type, resourceId);
        return ApiResponse.success(PageResponse.of(
                page.getRecords(), page.getTotal(), pageNum, pageSize));
    }
}
