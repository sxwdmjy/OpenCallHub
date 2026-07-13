package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.resource.service.INumberPoolService;
import com.och.resource.domain.NumberPool;
import com.och.security.audit.Audited;
import com.och.security.permission.RequiresPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 号码池管理控制器，提供号码池的CRUD操作。
 * 号码池用于将多个电话号码分组，支持灵活的路由策略。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "NumberPool", description = "号码池管理")
@RestController
@RequestMapping("/api/resource/number-pools")
@RequiredArgsConstructor
public class NumberPoolController {

    private final INumberPoolService numberPoolService;

    @GetMapping
    public ApiResponse<PageResponse<NumberPool>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(numberPoolService.getPageList(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<NumberPool> get(@PathVariable Long id) {
        return ApiResponse.success(numberPoolService.getDetail(id));
    }

    @PostMapping
    @Audited(action = "CREATE", targetType = "NUMBER_POOL", description = "创建号码池")
    @RequiresPermission(value = "resource:number-pool:create", description = "创建号码池")
    public ApiResponse<NumberPool> create(@RequestBody NumberPool pool) {
        return ApiResponse.success(numberPoolService.add(pool));
    }

    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "NUMBER_POOL", description = "更新号码池")
    @RequiresPermission(value = "resource:number-pool:update", description = "更新号码池")
    public ApiResponse<NumberPool> update(@PathVariable Long id, @RequestBody NumberPool pool) {
        return ApiResponse.success(numberPoolService.edit(id, pool));
    }

    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "NUMBER_POOL", description = "删除号码池")
    @RequiresPermission(value = "resource:number-pool:delete", description = "删除号码池")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        numberPoolService.delete(id);
        return ApiResponse.success();
    }
}
