package com.och.api.controller.customer;


import com.github.pagehelper.PageInfo;
import com.och.calltask.domain.query.CustomerFieldAddQuery;
import com.och.calltask.domain.query.CustomerFieldQuery;
import com.och.calltask.domain.vo.CustomerFieldVo;
import com.och.calltask.service.ICustomerFieldService;
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
 * 客户字段配置管理
 *
 * @author danmo
 * @date 2025/06/16 14:59
 */
@Tag(name = "客户字段配置管理")
@RestController
@RequestMapping("/customer/v1/field")
public class CustomerFieldController extends BaseController {

    @Autowired
    private ICustomerFieldService customerFieldService;


    @Log(title = "新增字段", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('customer:field:add')")
    @Operation(summary = "新增字段", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CustomerFieldAddQuery query) {
        customerFieldService.add(query);
        return success();
    }

    @Log(title = "修改字段", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('customer:field:edit')")
    @Operation(summary = "修改字段", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CustomerFieldAddQuery query) {
        query.setId(id);
        customerFieldService.edit(query);
        return success();
    }

    @Log(title = "字段详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:field:get')")
    @Operation(summary = "字段详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CustomerFieldVo> get(@PathVariable("id") Long id) {
        return success(customerFieldService.getDetail(id));
    }


    @Log(title = "删除字段", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('customer:field:delete')")
    @Operation(summary = "删除字段", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CustomerFieldQuery query) {
        customerFieldService.delete(query);
        return success();
    }

    @Log(title = "字段列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('customer:field:page:list')")
    @Operation(summary = "字段列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CustomerFieldVo>> pageList(@RequestBody CustomerFieldQuery query) {
        List<CustomerFieldVo> list = customerFieldService.pageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "字段列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "字段列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CustomerFieldVo>> list(@RequestBody CustomerFieldQuery query) {
        List<CustomerFieldVo> list = customerFieldService.getList(query);
        return success(list);
    }

}
