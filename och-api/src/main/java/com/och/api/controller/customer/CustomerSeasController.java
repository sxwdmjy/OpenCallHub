package com.och.api.controller.customer;

import lombok.RequiredArgsConstructor;


import com.och.customer.domain.CustomerProfile;
import com.och.customer.service.ICustomerLookupService;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 客户公海管理
 *
 * @author danmo
 * @date 2025/06/16 14:59
 */
@Tag(name = "客户公海管理")
@RestController
@RequestMapping("/customer/v1/seas")
@RequiredArgsConstructor
public class CustomerSeasController extends BaseController {
    private final ICustomerLookupService customerLookupService;

    @Log(title = "新增公海", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('customer:seas:add')")
    @Operation(summary = "新增公海", method = "POST")
    @PostMapping("/add")
    public ApiResponse add(@RequestBody CustomerProfile profile) {
        customerLookupService.addProfile(profile);
        return success();
    }

    @Log(title = "修改公海", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('customer:seas:edit')")
    @Operation(summary = "修改公海", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse edit(@PathVariable("id") Long id, @RequestBody CustomerProfile profile) {
        profile.setId(id);
        customerLookupService.updateProfile(profile);
        return success();
    }

    @Log(title = "公海详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:seas:get')")
    @Operation(summary = "公海详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<CustomerProfile> get(@PathVariable("id") Long id) {
        return success(customerLookupService.getProfile(id));
    }

    @Log(title = "删除公海", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('customer:seas:delete')")
    @Operation(summary = "删除公海", method = "POST")
    @PostMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable("id") Long id) {
        customerLookupService.deleteProfile(id);
        return success();
    }

    @Log(title = "公海列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:seas:page:list')")
    @Operation(summary = "公海列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<List<CustomerProfile>> pageList(@RequestBody CustomerProfile query) {
        return success(customerLookupService.listProfiles(query));
    }

    @Log(title = "公海列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "公海列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<CustomerProfile>> list(@RequestBody CustomerProfile query) {
        return success(customerLookupService.listProfiles(query));
    }

    @Log(title = "导入客户", businessType = BusinessTypeEnum.IMPORT)
    @PreAuthorize("@authz.hasPerm('customer:seas:import')")
    @Operation(summary = "导入客户", method = "POST")
    @PostMapping("/import/{templateId}")
    public ApiResponse importCustomer(@PathVariable("templateId") Long templateId, @RequestParam("file") MultipartFile file) {
        customerLookupService.importCustomers(file);
        return success();
    }

}
