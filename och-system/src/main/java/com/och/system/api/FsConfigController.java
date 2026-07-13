package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsConfig;
import com.och.system.domain.query.fsconfig.FsConfigAddQuery;
import com.och.system.domain.query.fsconfig.FsConfigQuery;
import com.och.system.service.IFsConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FreeSWITCH 配置管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "fs配置管理")
@RestController
@RequestMapping("/fs/v1/config")
@RequiredArgsConstructor
public class FsConfigController {

    private final IFsConfigService fsConfigService;

    @Log(title = "新增fs配置", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:fs:add')")
    @Operation(summary = "新增fs配置", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated FsConfigAddQuery query) {
        fsConfigService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改fs配置", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:fs:edit')")
    @Operation(summary = "修改fs配置", method = "PUT")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Integer id, @RequestBody @Validated FsConfigAddQuery query) {
        query.setId(id);
        fsConfigService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "fs配置详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:get')")
    @Operation(summary = "fs配置详情", method = "GET")
    @PostMapping("/get/{id}")
    public ApiResponse<FsConfig> get(@PathVariable("id") Integer id) {
        return ApiResponse.success(fsConfigService.getDetail(id));
    }

    @Log(title = "删除fs配置", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:fs:delete')")
    @Operation(summary = "删除fs配置", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody FsConfigQuery query) {
        fsConfigService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "fs配置列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:page:list')")
    @Operation(summary = "fs配置列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<FsConfig>> pageList(@RequestBody FsConfigQuery query) {
        List<FsConfig> list = fsConfigService.getPageList(query);
        PageInfo<FsConfig> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    @Log(title = "fs配置列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:list')")
    @Operation(summary = "fs配置列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<FsConfig>> list(@RequestBody FsConfigQuery query) {
        List<FsConfig> list = fsConfigService.getList(query);
        return ApiResponse.success(list);
    }
}
