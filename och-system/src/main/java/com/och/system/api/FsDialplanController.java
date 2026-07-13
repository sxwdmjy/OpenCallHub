package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsDialplan;
import com.och.system.domain.query.dialplan.FsDialplanAddQuery;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.vo.dialplan.FsDialplanVo;
import com.och.system.service.IFsDialplanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FreeSWITCH 拨号计划管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "FS拨号计划管理")
@RestController
@RequestMapping("/fs/v1/dialplan")
@RequiredArgsConstructor
public class FsDialplanController {

    private final IFsDialplanService fsDialplanService;

    @Log(title = "新增拨号计划", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:add')")
    @Operation(summary = "新增拨号计划", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated FsDialplanAddQuery query) {
        fsDialplanService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改拨号计划", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:dialplan:edit')")
    @Operation(summary = "修改拨号计划", method = "PUT")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Long id, @RequestBody @Validated FsDialplanAddQuery query) {
        query.setId(id);
        fsDialplanService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "拨号计划详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:get')")
    @Operation(summary = "拨号计划详情", method = "GET")
    @PostMapping("/get/{id}")
    public ApiResponse<FsDialplan> get(@PathVariable("id") Long id) {
        return ApiResponse.success(fsDialplanService.getDetail(id));
    }

    @Log(title = "删除拨号计划", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:dialplan:delete')")
    @Operation(summary = "删除拨号计划", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody FsDialplanQuery query) {
        fsDialplanService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "拨号计划列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:page:list')")
    @Operation(summary = "拨号计划列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<FsDialplanVo>> pageList(@RequestBody FsDialplanQuery query) {
        List<FsDialplanVo> list = fsDialplanService.getPageList(query);
        PageInfo<FsDialplanVo> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    @Log(title = "拨号计划列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:list')")
    @Operation(summary = "拨号计划列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<FsDialplanVo>> list(@RequestBody FsDialplanQuery query) {
        List<FsDialplanVo> list = fsDialplanService.getList(query);
        return ApiResponse.success(list);
    }
}
