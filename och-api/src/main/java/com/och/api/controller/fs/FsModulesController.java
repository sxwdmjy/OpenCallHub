package com.och.api.controller.fs;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsModules;
import com.och.system.domain.query.modules.FsModulesAddQuery;
import com.och.system.domain.query.modules.FsModulesQuery;
import com.och.system.service.IFsModulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2023年09月11日 13:37
 */
@Tag(name = "FS模块配置管理")
@RestController
@RequestMapping("/system/v1/modules")
public class FsModulesController extends BaseController {

    @Autowired
    private IFsModulesService iFsModulesService;

    @Log(title = "新增模块配置", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:add')")
    @Operation(summary = "新增模块配置", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated FsModulesAddQuery query) {
        iFsModulesService.add(query);
        return success();
    }

    @Log(title = "修改模块配置", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:edit')")
    @Operation(summary = "修改模块配置", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated FsModulesAddQuery query) {
        query.setId(id);
        iFsModulesService.edit(query);
        return success();
    }

    @Log(title = "模块配置详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:get')")
    @Operation(summary = "模块配置详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<FsModules> get(@PathVariable("id") Long id) {
        return success(iFsModulesService.getDetail(id));
    }


    @Log(title = "删除模块配置", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:delete')")
    @Operation(summary = "删除模块配置", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody FsModulesQuery query) {
        iFsModulesService.delete(query);
        return success();
    }

    @Log(title = "模块配置列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:page:list')")
    @Operation(summary = "模块配置列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<FsModules>> pageList(@RequestBody FsModulesQuery query) {
        List<FsModules> list = iFsModulesService.getPageList(query);
        PageInfo<FsModules> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

    @Log(title = "模块配置列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:list')")
    @Operation(summary = "模块配置列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<FsModules>> list(@RequestBody FsModulesQuery query) {
        List<FsModules> list = iFsModulesService.getList(query);
        return success(list);
    }

}
