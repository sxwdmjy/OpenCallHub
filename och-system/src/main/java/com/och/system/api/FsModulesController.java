package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsModules;
import com.och.system.domain.query.modules.FsModulesAddQuery;
import com.och.system.domain.query.modules.FsModulesQuery;
import com.och.system.service.IFsModulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FreeSWITCH 模块配置管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "FS模块配置管理")
@RestController
@RequestMapping("/fs/v1/modules")
@RequiredArgsConstructor
public class FsModulesController {

    private final IFsModulesService fsModulesService;

    @Log(title = "新增模块配置", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:add')")
    @Operation(summary = "新增模块配置", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated FsModulesAddQuery query) {
        fsModulesService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改模块配置", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:edit')")
    @Operation(summary = "修改模块配置", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Long id, @RequestBody @Validated FsModulesAddQuery query) {
        query.setId(id);
        fsModulesService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "模块配置详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:get')")
    @Operation(summary = "模块配置详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<FsModules> get(@PathVariable("id") Long id) {
        return ApiResponse.success(fsModulesService.getDetail(id));
    }

    @Log(title = "删除模块配置", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:delete')")
    @Operation(summary = "删除模块配置", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody FsModulesQuery query) {
        fsModulesService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "模块配置列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:page:list')")
    @Operation(summary = "模块配置列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<FsModules>> pageList(@RequestBody FsModulesQuery query) {
        List<FsModules> list = fsModulesService.getPageList(query);
        PageInfo<FsModules> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    @Log(title = "模块配置列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:modules:list')")
    @Operation(summary = "模块配置列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<FsModules>> list(@RequestBody FsModulesQuery query) {
        List<FsModules> list = fsModulesService.getList(query);
        return ApiResponse.success(list);
    }
}
