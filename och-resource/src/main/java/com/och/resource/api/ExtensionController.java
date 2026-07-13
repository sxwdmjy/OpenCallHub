package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.resource.service.IExtensionService;
import com.och.resource.domain.Extension;
import com.och.security.audit.Audited;
import com.och.security.permission.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 分机管理控制器，提供分机的CRUD操作。
 * 分机是电话系统中内部分机号的管理入口。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "Extension", description = "分机管理")
@RestController
@RequestMapping("/api/resource/extensions")
@RequiredArgsConstructor
public class ExtensionController {

    private final IExtensionService extensionService;

    @GetMapping
    public ApiResponse<PageResponse<Extension>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(extensionService.getPageList(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<Extension> get(@PathVariable Long id) {
        return ApiResponse.success(extensionService.getDetail(id));
    }

    @PostMapping
    @Audited(action = "CREATE", targetType = "EXTENSION", description = "创建分机")
    @RequiresPermission(value = "resource:extension:create", description = "创建分机")
    public ApiResponse<Extension> create(@RequestBody Extension ext) {
        return ApiResponse.success(extensionService.add(ext));
    }

    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "EXTENSION", description = "更新分机")
    @RequiresPermission(value = "resource:extension:update", description = "更新分机")
    public ApiResponse<Extension> update(@PathVariable Long id, @RequestBody Extension ext) {
        return ApiResponse.success(extensionService.edit(id, ext));
    }

    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "EXTENSION", description = "删除分机")
    @RequiresPermission(value = "resource:extension:delete", description = "删除分机")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        extensionService.delete(id);
        return ApiResponse.success();
    }
}
