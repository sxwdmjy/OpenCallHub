package com.och.api.controller.customer;


import com.github.pagehelper.PageInfo;
import com.och.calltask.domain.query.CustomerSeasAddQuery;
import com.och.calltask.domain.query.CustomerSeasQuery;
import com.och.calltask.domain.vo.CustomerSeasVo;
import com.och.calltask.service.ICustomerSeasService;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
public class CustomerSeasController extends BaseController {

    @Autowired
    private ICustomerSeasService customerSeasService;


    @Log(title = "新增公海", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('customer:seas:add')")
    @Operation(summary = "新增公海", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CustomerSeasAddQuery query) {
        customerSeasService.add(query);
        return success();
    }

    @Log(title = "修改公海", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('customer:seas:edit')")
    @Operation(summary = "修改公海", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CustomerSeasAddQuery query) {
        query.setId(id);
        customerSeasService.edit(query);
        return success();
    }

    @Log(title = "公海详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:seas:get')")
    @Operation(summary = "公海详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CustomerSeasVo> get(@PathVariable("id") Long id) {
        return success(customerSeasService.getDetail(id));
    }


    @Log(title = "删除公海", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('customer:seas:delete')")
    @Operation(summary = "删除公海", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CustomerSeasQuery query) {
        customerSeasService.delete(query);
        return success();
    }

    @Log(title = "公海列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:seas:page:list')")
    @Operation(summary = "公海列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CustomerSeasVo>> pageList(@RequestBody CustomerSeasQuery query) {
        List<CustomerSeasVo> list = customerSeasService.pageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "公海列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "公海列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CustomerSeasVo>> list(@RequestBody CustomerSeasQuery query) {
        List<CustomerSeasVo> list = customerSeasService.getList(query);
        return success(list);
    }

}
