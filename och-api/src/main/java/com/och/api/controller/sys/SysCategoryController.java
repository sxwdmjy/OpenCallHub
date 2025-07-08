package com.och.api.controller.sys;

import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.SysCategory;
import com.och.system.domain.query.category.SysCategoryAddQuery;
import com.och.system.domain.query.category.SysCategoryQuery;
import com.och.system.service.ISysCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author danmo
 * @date 2023-10-31 11:14
 **/
@Tag(name = "分类管理")
@RestController
@RequestMapping("/system/v1/category")
public class SysCategoryController extends BaseController {

    @Autowired
    private ISysCategoryService iSysCategoryService;

    @Log(title = "新增类目",businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:category:add')")
    @PostMapping("/add")
    @Operation(summary = "新增类目", method = "POST")
    public ResResult add(@Validated @RequestBody SysCategoryAddQuery query) {
        iSysCategoryService.add(query);
        return success();
    }

    @Log(title = "修改类目",businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:category:edit')")
    @PostMapping("/edit/{id}")
    @Operation(summary = "修改类目", method = "POST")
    public ResResult edit(@PathVariable("id") Long id, @Validated @RequestBody SysCategoryAddQuery query) {
        query.setId(id);
        iSysCategoryService.edit(query);
        return success();
    }

    @Log(title = "删除类目",businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:category:delete')")
    @PostMapping("/delete")
    @Operation(summary = "删除类目", method = "POST")
    public ResResult delete(@RequestBody SysCategoryQuery query) {
        iSysCategoryService.delete(query);
        return success();
    }

    @Log(title = "类目详情",businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:category:get')")
    @PostMapping(value = "/get/{id}")
    @Operation(summary = "类目详情", method = "POST")
    public ResResult<SysCategory> getInfo(@PathVariable("id") Long id) {
        return success(iSysCategoryService.getDetail(id));
    }

    @Log(title = "类目树",businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:category:tree:list')")
    @PostMapping("/tree/list")
    @Operation(summary = "类目树", method = "POST")
    public ResResult<SysCategory> treeList(@RequestBody SysCategoryQuery query) {
        return success(iSysCategoryService.treeList(query));
    }
}
