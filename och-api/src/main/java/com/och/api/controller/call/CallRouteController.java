package com.och.api.controller.call;

import lombok.RequiredArgsConstructor;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.route.CallRouteAddQuery;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.service.ICallRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "号码路由管理")
@RestController
@RequestMapping("call/v1/route")
@RequiredArgsConstructor
public class CallRouteController extends BaseController {
    private final ICallRouteService iCallRouteService;

    @Log(title = "新增号码路由", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:rout:add')")
    @Operation(summary = "新增号码路由", method = "POST")
    @PostMapping("/add")
    public ApiResponse add(@RequestBody @Validated CallRouteAddQuery query) {
        iCallRouteService.add(query);
        return success();
    }

    @Log(title = "修改号码路由", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:rout:edit')")
    @Operation(summary = "修改号码路由", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse edit(@PathVariable("id") Long id, @RequestBody @Validated CallRouteAddQuery query) {
        query.setId(id);
        iCallRouteService.edit(query);
        return success();
    }

    @Log(title = "启用路由" , businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:rout:enable')")
    @Operation(summary = "启用路由", method = "POST")
    @PostMapping("/enable/{id}")
    public ApiResponse enable(@PathVariable("id") Long id) {
        iCallRouteService.enable(id);
        return success();
    }

    @Log(title = "禁用路由" , businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:rout:disable')")
    @Operation(summary = "禁用路由", method = "POST")
    @PostMapping("/disable/{id}")
    public ApiResponse disable(@PathVariable("id") Long id) {
        iCallRouteService.disable(id);
        return success();
    }

    @Log(title = "删除号码路由", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:rout:delete')")
    @Operation(summary = "删除号码路由", method = "POST")
    @PostMapping("/delete")
    public ApiResponse delete(@RequestBody CallRouteQuery query) {
        iCallRouteService.delete(query);
        return success();
    }

    @Log(title = "号码路由详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:rout:get')")
    @Operation(summary = "号码路由详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<CallRouteVo> get(@PathVariable("id") Long id) {
        return success(iCallRouteService.getDetail(id));
    }

    @Log(title = "号码路由列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:rout:page:list')")
    @Operation(summary = "号码路由列表", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<CallRouteVo>> pageList(@RequestBody CallRouteQuery query) {
        List<CallRouteVo> list = iCallRouteService.getPageList(query);
        PageInfo<CallRouteVo> pageInfo = PageInfo.of(list);
        return success(pageInfo);
    }

}
