package com.och.customer.api;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.customer.service.ICustomerLookupService;
import com.och.customer.service.impl.CustomerLookupServiceImpl;
import com.och.customer.domain.CustomerPhone;
import com.och.customer.domain.CustomerPool;
import com.och.customer.domain.CustomerFollowUp;
import com.och.customer.domain.CustomerProfile;
import org.springframework.security.access.prepost.PreAuthorize;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Customer", description = "客户查询")
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerLookupController {

    private final ICustomerLookupService lookupService;

    @Operation(summary = "按电话号码查找客户")
    @GetMapping("/lookup/by-phone")
    @Audited(action = "CUSTOMER_LOOKUP", targetType = "CUSTOMER")
    public ApiResponse<Map<String, Object>> lookupByPhone(@RequestParam String phone) {
        CustomerProfile profile = lookupService.findByPhone(phone);
        Map<String, Object> result = new HashMap<>();
        if (profile != null) {
            List<CustomerPhone> phones = lookupService.getPhones(profile.getId());
            phones.forEach(p -> p.setPhoneNumber(CustomerLookupServiceImpl.maskPhone(p.getPhoneNumber())));
            result.put("customer", profile);
            result.put("phones", phones);
            result.put("found", true);
        } else {
            result.put("found", false);
        }
        return ApiResponse.success(result);
    }

    // ======================== 客户档案CRUD ========================

    @Operation(summary = "客户分页列表")
    @GetMapping("/page")
    public ApiResponse<PageResponse<CustomerProfile>> pageProfiles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<CustomerProfile> page = lookupService.pageProfiles(pageNum, pageSize, keyword);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @Operation(summary = "客户详情")
    @GetMapping("/{id}")
    public ApiResponse<CustomerProfile> getProfile(@PathVariable Long id) {
        return ApiResponse.success(lookupService.getProfile(id));
    }

    @Operation(summary = "新增客户")
    @PostMapping
    @Audited(action = "CREATE_CUSTOMER", targetType = "CUSTOMER")
    public ApiResponse<Void> addProfile(@RequestBody CustomerProfile profile) {
        lookupService.addProfile(profile);
        return ApiResponse.success();
    }

    @Operation(summary = "更新客户")
    @PutMapping
    @Audited(action = "UPDATE_CUSTOMER", targetType = "CUSTOMER")
    public ApiResponse<Void> updateProfile(@RequestBody CustomerProfile profile) {
        lookupService.updateProfile(profile);
        return ApiResponse.success();
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    @Audited(action = "DELETE_CUSTOMER", targetType = "CUSTOMER")
    public ApiResponse<Void> deleteProfile(@PathVariable Long id) {
        lookupService.deleteProfile(id);
        return ApiResponse.success();
    }

    // ======================== 客户池管理 ========================

    @Operation(summary = "客户池列表")
    @GetMapping("/pools")
    public ApiResponse<PageResponse<CustomerPool>> pagePools(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<CustomerPool> page = lookupService.pagePools(pageNum, pageSize);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @Operation(summary = "新增客户池")
    @PostMapping("/pools")
    @Audited(action = "CREATE_POOL", targetType = "CUSTOMER_POOL")
    public ApiResponse<Void> addPool(@RequestBody CustomerPool pool) {
        lookupService.addPool(pool);
        return ApiResponse.success();
    }

    @Operation(summary = "更新客户池")
    @PutMapping("/pools")
    @Audited(action = "UPDATE_POOL", targetType = "CUSTOMER_POOL")
    public ApiResponse<Void> updatePool(@RequestBody CustomerPool pool) {
        lookupService.updatePool(pool);
        return ApiResponse.success();
    }

    @Operation(summary = "删除客户池")
    @DeleteMapping("/pools/{id}")
    @Audited(action = "DELETE_POOL", targetType = "CUSTOMER_POOL")
    public ApiResponse<Void> deletePool(@PathVariable Long id) {
        lookupService.deletePool(id);
        return ApiResponse.success();
    }

    @Operation(summary = "客户跟进记录")
    @GetMapping("/{id}/follow-ups")
    public ApiResponse<List<CustomerFollowUp>> getFollowUps(@PathVariable Long id) {
        return ApiResponse.success(lookupService.getFollowUps(id));
    }

    @Operation(summary = "新增跟进记录")
    @PreAuthorize("@authz.hasPerm('customer:followup:add')")
    @PostMapping("/follow-up")
    @Audited(action = "ADD_FOLLOW_UP", targetType = "CUSTOMER")
    public ApiResponse<CustomerFollowUp> addFollowUp(@RequestBody CustomerFollowUp followUp) {
        return ApiResponse.success(lookupService.addFollowUp(followUp));
    }
}
