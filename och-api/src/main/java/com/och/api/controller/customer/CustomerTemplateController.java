package com.och.api.controller.customer;


import com.github.pagehelper.PageInfo;
import com.och.calltask.domain.query.CustomerTemplateAddQuery;
import com.och.calltask.domain.query.CustomerTemplateQuery;
import com.och.calltask.domain.vo.CustomerTemplateVo;
import com.och.calltask.service.ICustomerTemplateService;
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
 * 客户模板配置管理
 *
 * @author danmo
 * @date 2025/06/16 14:59
 */
@Tag(name = "客户模板配置管理")
@RestController
@RequestMapping("/customer/v1/template")
public class CustomerTemplateController extends BaseController {

    @Autowired
    private ICustomerTemplateService customerTemplateService;


    @Log(title = "新增模板", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('customer:template:add')")
    @Operation(summary = "新增模板", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CustomerTemplateAddQuery query) {
        customerTemplateService.add(query);
        return success();
    }

    @Log(title = "修改模板", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('customer:template:edit')")
    @Operation(summary = "修改模板", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CustomerTemplateAddQuery query) {
        query.setId(id);
        customerTemplateService.edit(query);
        return success();
    }

    @Log(title = "模板详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:template:get')")
    @Operation(summary = "模板详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CustomerTemplateVo> get(@PathVariable("id") Long id) {
        return success(customerTemplateService.getDetail(id));
    }


    @Log(title = "删除模板", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('customer:template:delete')")
    @Operation(summary = "删除模板", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CustomerTemplateQuery query) {
        customerTemplateService.delete(query);
        return success();
    }

    @Log(title = "模板列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:template:page:list')")
    @Operation(summary = "模板列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CustomerTemplateVo>> pageList(@RequestBody CustomerTemplateQuery query) {
        PageInfo<CustomerTemplateVo> list = customerTemplateService.pageList(query);
        return success(list);
    }

    @Log(title = "模板列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "模板列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CustomerTemplateVo>> list(@RequestBody CustomerTemplateQuery query) {
        List<CustomerTemplateVo> list = customerTemplateService.getList(query);
        return success(list);
    }

}
