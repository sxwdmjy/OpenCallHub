package com.och.api.controller.customer;


import com.github.pagehelper.PageInfo;
import com.och.calltask.domain.query.CustomerCrowdAddQuery;
import com.och.calltask.domain.query.CustomerCrowdQuery;
import com.och.calltask.domain.vo.CustomerCrowdVo;
import com.och.calltask.service.ICustomerCrowdService;
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
 * 客户人群管理
 *
 * @author danmo
 * @date 2025/06/16 14:59
 */
@Tag(name = "客户人群管理")
@RestController
@RequestMapping("/customer/v1/crowd")
public class CustomerCrowdController extends BaseController {

    @Autowired
    private ICustomerCrowdService customerCrowdService;


    @Log(title = "新增人群", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('customer:crowd:add')")
    @Operation(summary = "新增人群", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CustomerCrowdAddQuery query) {
        customerCrowdService.add(query);
        return success();
    }

    @Log(title = "修改人群", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('customer:crowd:edit')")
    @Operation(summary = "修改人群", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CustomerCrowdAddQuery query) {
        query.setId(id);
        customerCrowdService.edit(query);
        return success();
    }

    @Log(title = "人群详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:crowd:get')")
    @Operation(summary = "人群详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CustomerCrowdVo> get(@PathVariable("id") Long id) {
        return success(customerCrowdService.getDetail(id));
    }


    @Log(title = "删除人群", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('customer:crowd:delete')")
    @Operation(summary = "删除人群", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CustomerCrowdQuery query) {
        customerCrowdService.delete(query);
        return success();
    }

    @Log(title = "人群列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:crowd:page:list')")
    @Operation(summary = "人群列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CustomerCrowdVo>> pageList(@RequestBody CustomerCrowdQuery query) {
        List<CustomerCrowdVo> list = customerCrowdService.pageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "人群列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "人群列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CustomerCrowdVo>> list(@RequestBody CustomerCrowdQuery query) {
        List<CustomerCrowdVo> list = customerCrowdService.getList(query);
        return success(list);
    }

}
