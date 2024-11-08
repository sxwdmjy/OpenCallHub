package com.och.api.controller;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsDialplan;
import com.och.system.domain.query.dialplan.FsDialplanAddQuery;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.vo.dialplan.FsDialplanVo;
import com.och.system.service.IFsDialplanService;
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
@Tag(name = "FS拨号计划管理")
@RestController
@RequestMapping("/system/v1/dialplan")
public class FsDialplanController extends BaseController {

    @Autowired
    private IFsDialplanService iFsDialplanService;

    @Log(title = "新增拨号计划", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:add')")
    @Operation(summary = "新增拨号计划", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated FsDialplanAddQuery query) {
        iFsDialplanService.add(query);
        return success();
    }

    @Log(title = "修改拨号计划", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:dialplan:edit')")
    @Operation(summary = "修改拨号计划", method = "PUT")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated FsDialplanAddQuery query) {
        query.setId(id);
        iFsDialplanService.edit(query);
        return success();
    }
    @Log(title = "拨号计划详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:get')")
    @Operation(summary = "拨号计划详情", method = "GET")
    @PostMapping("/get/{id}")
    public ResResult<FsDialplan> get(@PathVariable("id") Long id) {
        return success(iFsDialplanService.getDetail(id));
    }

    @Log(title = "删除拨号计划", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:dialplan:delete')")
    @Operation(summary = "删除拨号计划", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody FsDialplanQuery query) {
        iFsDialplanService.delete(query);
        return success();
    }

    @Log(title = "拨号计划列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:page:list')")
    @Operation(summary = "拨号计划列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<FsDialplanVo>> pageList(@RequestBody FsDialplanQuery query) {
        List<FsDialplanVo> list = iFsDialplanService.getPageList(query);
        PageInfo<FsDialplanVo> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

    @Log(title = "拨号计划列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dialplan:list')")
    @Operation(summary = "拨号计划列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<FsDialplanVo>> list(@RequestBody FsDialplanQuery query) {
        List<FsDialplanVo> list = iFsDialplanService.getList(query);
        return success(list);
    }

}
