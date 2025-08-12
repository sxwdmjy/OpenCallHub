package com.och.api.controller.call;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.CallEngine;
import com.och.system.domain.query.engine.CallEngineAddQuery;
import com.och.system.domain.query.engine.CallEngineQuery;
import com.och.system.service.ICallEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2025年02月09日 13:37
 */
@Tag(name = "引擎管理")
@RestController
@RequestMapping("/call/v1/engine")
public class CallEngineController extends BaseController {

    @Autowired
    private ICallEngineService callEngineService;

    @Log(title = "新增引擎", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:engine:add')")
    @Operation(summary = "新增引擎", method = "POST")
    @PostMapping("/add")
    public ResResult addEngine(@RequestBody @Validated CallEngineAddQuery query) {
        callEngineService.addEngine(query);
        return success();
    }

    @Log(title = "修改引擎", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:engine:edit')")
    @Operation(summary = "修改引擎", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CallEngineAddQuery query) {
        query.setId(id);
        callEngineService.edit(query);
        return success();
    }

    @Log(title = "删除引擎", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:engine:delete')")
    @Operation(summary = "删除引擎", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CallEngineQuery query) {
        callEngineService.delete(query);
        return success();
    }

    @Log(title = "引擎详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:engine:get')")
    @Operation(summary = "引擎详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CallEngine> getDetail(@PathVariable("id") Long id) {
        return success(callEngineService.getDetail(id));
    }

    @Log(title = "引擎列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:engine:page:list')")
    @Operation(summary = "引擎列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CallEngine>> pageList(@RequestBody CallEngineQuery query) {
        List<CallEngine> pageList = callEngineService.getPageList(query);
        PageInfo<CallEngine> pageInfo = PageInfo.of(pageList);
        return success(pageInfo);
    }

    @Operation(summary = "引擎列表", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CallEngine>> getList(@RequestBody CallEngineQuery query) {
        List<CallEngine> list = callEngineService.getList(query);
        return success(list);
    }


}
