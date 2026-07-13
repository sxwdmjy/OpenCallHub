package com.och.api.controller.call;

import lombok.RequiredArgsConstructor;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.display.CallDisplayPoolAddQuery;
import com.och.system.domain.query.display.CallDisplayPoolQuery;
import com.och.system.domain.vo.display.CallDisplayPoolVo;
import com.och.system.service.ICallDisplayPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2024年08月09日 13:37
 */
@Tag(name = "号码池管理")
@RestController
@RequestMapping("/call/v1/display/pool/")
@RequiredArgsConstructor
public class CallDisplayPoolController extends BaseController {
    private final ICallDisplayPoolService iCallDisplayPoolService;


    @Log(title = "新增号码池", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:display:pool:add')")
    @Operation(summary = "新增号码池", method = "POST")
    @PostMapping("/add")
    public ApiResponse addPool(@RequestBody @Validated CallDisplayPoolAddQuery query) {
        iCallDisplayPoolService.addPool(query);
        return success();
    }

    @Log(title = "修改号码池", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:display:pool:edit')")
    @Operation(summary = "修改号码池", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse editPool(@PathVariable("id") Long id, @RequestBody @Validated CallDisplayPoolAddQuery query) {
        query.setId(id);
        iCallDisplayPoolService.editPool(query);
        return success();
    }

    @Log(title = "删除号码池", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:display:pool:delete')")
    @Operation(summary = "删除号码池", method = "POST")
    @PostMapping("/delete")
    public ApiResponse deletePool(@RequestBody CallDisplayPoolQuery query) {
        iCallDisplayPoolService.deletePool(query);
        return success();
    }

    @Log(title = "号码池详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:pool:get')")
    @Operation(summary = "号码池详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<CallDisplayPoolVo> getPoolDetail(@PathVariable("id") Long id) {
        return success(iCallDisplayPoolService.getPoolDetail(id));
    }

    @Log(title = "号码池列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:pool:page:list')")
    @Operation(summary = "号码池列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<CallDisplayPoolVo>> pagePoolList(@RequestBody CallDisplayPoolQuery query) {
        PageInfo<CallDisplayPoolVo> list = iCallDisplayPoolService.pagePoolList(query);
        return success(list);
    }

    @Operation(summary = "号码池列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<CallDisplayPoolVo>> getPoolList(@RequestBody CallDisplayPoolQuery query) {
        List<CallDisplayPoolVo> list = iCallDisplayPoolService.getPoolList(query);
        return success(list);
    }
}
