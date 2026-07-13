package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.resource.service.IPhoneNumberService;
import com.och.resource.domain.PhoneNumber;
import com.och.security.audit.Audited;
import com.och.security.permission.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 电话号码管理控制器，提供外线电话号码的CRUD操作。
 * 支持号码归属查询、号码池关联等功能。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "PhoneNumber", description = "电话号码管理")
@RestController
@RequestMapping("/api/resource/phone-numbers")
@RequiredArgsConstructor
public class PhoneNumberController {

    private final IPhoneNumberService phoneNumberService;

    @GetMapping
    public ApiResponse<PageResponse<PhoneNumber>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(phoneNumberService.getPageList(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<PhoneNumber> get(@PathVariable Long id) {
        return ApiResponse.success(phoneNumberService.getDetail(id));
    }

    @PostMapping
    @Audited(action = "CREATE", targetType = "PHONE_NUMBER", description = "创建电话号码")
    @RequiresPermission(value = "resource:phone-number:create", description = "创建电话号码")
    public ApiResponse<PhoneNumber> create(@RequestBody PhoneNumber phone) {
        return ApiResponse.success(phoneNumberService.add(phone));
    }

    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "PHONE_NUMBER", description = "更新电话号码")
    @RequiresPermission(value = "resource:phone-number:update", description = "更新电话号码")
    public ApiResponse<PhoneNumber> update(@PathVariable Long id, @RequestBody PhoneNumber phone) {
        return ApiResponse.success(phoneNumberService.edit(id, phone));
    }

    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "PHONE_NUMBER", description = "删除电话号码")
    @RequiresPermission(value = "resource:phone-number:delete", description = "删除电话号码")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        phoneNumberService.delete(id);
        return ApiResponse.success();
    }
}
