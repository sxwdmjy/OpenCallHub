package com.och.api.controller.fs;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsConfig;
import com.och.system.domain.query.fsconfig.FsConfigAddQuery;
import com.och.system.domain.query.fsconfig.FsConfigQuery;
import com.och.system.service.IFsConfigService;
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
@Tag(name = "fs配置管理")
@RestController
@RequestMapping("/system/v1/fs")
public class FsConfigController extends BaseController {

    @Autowired
    private IFsConfigService iFsConfigService;

    @Log(title = "新增fs配置", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:fs:add')")
    @Operation(summary = "新增fs配置", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated FsConfigAddQuery query) {
        iFsConfigService.add(query);
        return success();
    }

    @Log(title = "修改fs配置", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:fs:edit')")
    @Operation(summary = "修改fs配置", method = "PUT")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Integer id, @RequestBody @Validated FsConfigAddQuery query) {
        query.setId(id);
        iFsConfigService.edit(query);
        return success();
    }

    @Log(title = "fs配置详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:get')")
    @Operation(summary = "fs配置详情", method = "GET")
    @PostMapping("/get/{id}")
    public ResResult<FsConfig> get(@PathVariable("id") Integer id) {
        return success(iFsConfigService.getDetail(id));
    }

    @Log(title = "删除fs配置", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:fs:delete')")
    @Operation(summary = "删除fs配置", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody FsConfigQuery query) {
        iFsConfigService.delete(query);
        return success();
    }

    @Log(title = "fs配置列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:page:list')")
    @Operation(summary = "fs配置列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<FsConfig>> pageList(@RequestBody FsConfigQuery query) {

        List<FsConfig> list = iFsConfigService.getPageList(query);
        PageInfo<FsConfig> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

    @Log(title = "fs配置列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:list')")
    @Operation(summary = "fs配置列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<FsConfig>> list(@RequestBody FsConfigQuery query) {
        List<FsConfig> list = iFsConfigService.getList(query);
        return success(list);
    }

}
