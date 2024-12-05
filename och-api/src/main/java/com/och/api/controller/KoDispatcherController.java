package com.och.api.controller;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.KoDispatcher;
import com.och.system.domain.query.dispatcher.KoDispatcherAddQuery;
import com.och.system.domain.query.dispatcher.KoDispatcherQuery;
import com.och.system.service.IKoDispatcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author danmo
 * @date 2024年07月11日 13:37
 */
@Tag(name = "KO负载管理")
@RestController
@RequestMapping("/system/v1/dispatcher")
public class KoDispatcherController extends BaseController {

    @Autowired
    private IKoDispatcherService dispatcherService;


    @Log(title = "新增负载", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:add')")
    @Operation(summary = "新增负载", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated KoDispatcherAddQuery query) {
        dispatcherService.add(query);
        return success();
    }

    @Log(title = "修改负载", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:edit')")
    @Operation(summary = "修改负载", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Integer id, @RequestBody @Validated KoDispatcherAddQuery query) {
        query.setId(id);
        dispatcherService.edit(query);
        return success();
    }

    @Log(title = "负载详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:get')")
    @Operation(summary = "负载详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<KoDispatcher> get(@PathVariable("id") Integer id) {
        return success(dispatcherService.getDetail(id));
    }

    @Log(title = "删除负载", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:del')")
    @Operation(summary = "删除负载", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody KoDispatcherQuery query) {
        dispatcherService.delete(query);
        return success();
    }

    @Log(title = "负载列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:page:list')")
    @Operation(summary = "负载列表", method = "POST")
    @PostMapping("/page/list")
    public ResResult<List<KoDispatcher>> list(@RequestBody KoDispatcherQuery query) {
        List<KoDispatcher> list = dispatcherService.getPageList(query);
        PageInfo<KoDispatcher> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

}
