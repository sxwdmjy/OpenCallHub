package com.och.api.controller;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.display.CallDisplayAddQuery;
import com.och.system.domain.query.display.CallDisplayQuery;
import com.och.system.domain.vo.display.CallDisplaySimpleVo;
import com.och.system.domain.vo.display.CallDisplayVo;
import com.och.system.service.ICallDisplayService;
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
@Tag(name = "号码管理")
@RestController
@RequestMapping("/call/v1/display")
public class CallDisplayController extends BaseController {

    @Autowired
    private ICallDisplayService iCallDisplayService;

    @Log(title = "新增号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:display:add')")
    @Operation(summary = "新增号码", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CallDisplayAddQuery query) {
        iCallDisplayService.add(query);
        return success();
    }

    @Log(title = "修改号码", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:display:edit')")
    @Operation(summary = "修改号码", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CallDisplayAddQuery query) {
        query.setId(id);
        iCallDisplayService.edit(query);
        return success();
    }

    @Log(title = "号码详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:get')")
    @Operation(summary = "号码详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CallDisplayVo> get(@PathVariable("id") Long id) {
        return success(iCallDisplayService.getDetail(id));
    }

    @Log(title = "删除号码", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:display:delete')")
    @Operation(summary = "删除号码", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CallDisplayQuery query) {
        iCallDisplayService.delete(query);
        return success();
    }

    @Log(title = "号码列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:page:list')")
    @Operation(summary = "号码列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CallDisplayVo>> pageList(@RequestBody CallDisplayQuery query) {
        List<CallDisplayVo> list = iCallDisplayService.getPageList(query);
        return success(new PageInfo<>(list));
    }


    @Operation(summary = "号码列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CallDisplayVo>> list(@RequestBody CallDisplayQuery query) {
        return success(iCallDisplayService.getList(query));
    }

    @Operation(summary = "号码下拉列表", method = "POST")
    @PostMapping("/select/list")
    public ResResult<List<CallDisplaySimpleVo>> selectSimpleList(@RequestBody CallDisplayQuery query) {
        return success(iCallDisplayService.selectSimpleList(query));
    }
}
