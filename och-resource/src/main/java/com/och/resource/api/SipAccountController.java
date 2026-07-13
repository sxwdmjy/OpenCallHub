package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.resource.service.ISipAccountService;
import com.och.resource.domain.SipAccount;
import com.och.security.audit.Audited;
import com.och.security.permission.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * SIP账号管理控制器，提供SIP账号的CRUD操作。
 * 支持分页查询、详情查看、创建、更新和删除。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "SipAccount", description = "SIP账号管理")
@RestController
@RequestMapping("/api/resource/sip-accounts")
@RequiredArgsConstructor
public class SipAccountController {

    private final ISipAccountService sipAccountService;

    @Operation(summary = "分页查询SIP账号")
    @GetMapping
    public ApiResponse<PageResponse<SipAccount>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(sipAccountService.getPageList(pageNum, pageSize, keyword));
    }

    @Operation(summary = "获取SIP账号详情")
    @GetMapping("/{id}")
    public ApiResponse<SipAccount> get(@PathVariable Long id) {
        return ApiResponse.success(sipAccountService.getDetail(id));
    }

    @Operation(summary = "创建SIP账号")
    @PostMapping
    @Audited(action = "CREATE", targetType = "SIP_ACCOUNT", description = "创建SIP账号")
    @RequiresPermission(value = "resource:sip-account:create", description = "创建SIP账号")
    public ApiResponse<SipAccount> create(@RequestBody SipAccount account) {
        return ApiResponse.success(sipAccountService.add(account));
    }

    @Operation(summary = "更新SIP账号")
    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "SIP_ACCOUNT", description = "更新SIP账号")
    @RequiresPermission(value = "resource:sip-account:update", description = "更新SIP账号")
    public ApiResponse<SipAccount> update(@PathVariable Long id, @RequestBody SipAccount account) {
        return ApiResponse.success(sipAccountService.edit(id, account));
    }

    @Operation(summary = "删除SIP账号")
    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "SIP_ACCOUNT", description = "删除SIP账号")
    @RequiresPermission(value = "resource:sip-account:delete", description = "删除SIP账号")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sipAccountService.delete(id);
        return ApiResponse.success();
    }
}
