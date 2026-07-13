package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.display.CallDisplayAddQuery;
import com.och.system.domain.query.display.CallDisplayPoolAddQuery;
import com.och.system.domain.query.display.CallDisplayPoolQuery;
import com.och.system.domain.query.display.CallDisplayQuery;
import com.och.system.domain.vo.display.CallDisplayPoolVo;
import com.och.system.domain.vo.display.CallDisplaySimpleVo;
import com.och.system.domain.vo.display.CallDisplayVo;
import com.och.system.service.ICallDisplayPoolService;
import com.och.system.service.ICallDisplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 号码管理（号码显示、号码池）
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "号码管理")
@RestController
@RequestMapping("/call/v1/display")
@RequiredArgsConstructor
public class CallDisplayController {

    private final ICallDisplayService callDisplayService;
    private final ICallDisplayPoolService callDisplayPoolService;

    // ==================== 号码管理 ====================

    @Log(title = "新增号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:display:add')")
    @Operation(summary = "新增号码", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated CallDisplayAddQuery query) {
        callDisplayService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改号码", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:display:edit')")
    @Operation(summary = "修改号码", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Long id, @RequestBody @Validated CallDisplayAddQuery query) {
        query.setId(id);
        callDisplayService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "号码详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:get')")
    @Operation(summary = "号码详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<CallDisplayVo> get(@PathVariable("id") Long id) {
        return ApiResponse.success(callDisplayService.getDetail(id));
    }

    @Log(title = "删除号码", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:display:delete')")
    @Operation(summary = "删除号码", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody CallDisplayQuery query) {
        callDisplayService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "号码列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:page:list')")
    @Operation(summary = "号码列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<CallDisplayVo>> pageList(@RequestBody CallDisplayQuery query) {
        List<CallDisplayVo> list = callDisplayService.getPageList(query);
        return ApiResponse.success(new PageInfo<>(list));
    }

    @Operation(summary = "号码列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<CallDisplayVo>> list(@RequestBody CallDisplayQuery query) {
        return ApiResponse.success(callDisplayService.getList(query));
    }

    @Operation(summary = "号码下拉列表", method = "POST")
    @PostMapping("/select/list")
    public ApiResponse<List<CallDisplaySimpleVo>> selectSimpleList(@RequestBody CallDisplayQuery query) {
        return ApiResponse.success(callDisplayService.selectSimpleList(query));
    }

    // ==================== 号码池管理 ====================

    @Log(title = "新增号码池", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:display:pool:add')")
    @Operation(summary = "新增号码池", method = "POST")
    @PostMapping("/pool/add")
    public ApiResponse<Void> addPool(@RequestBody @Validated CallDisplayPoolAddQuery query) {
        callDisplayPoolService.addPool(query);
        return ApiResponse.success();
    }

    @Log(title = "修改号码池", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:display:pool:edit')")
    @Operation(summary = "修改号码池", method = "POST")
    @PostMapping("/pool/edit/{id}")
    public ApiResponse<Void> editPool(@PathVariable("id") Long id, @RequestBody @Validated CallDisplayPoolAddQuery query) {
        query.setId(id);
        callDisplayPoolService.editPool(query);
        return ApiResponse.success();
    }

    @Log(title = "删除号码池", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:display:pool:delete')")
    @Operation(summary = "删除号码池", method = "POST")
    @PostMapping("/pool/delete")
    public ApiResponse<Void> deletePool(@RequestBody CallDisplayPoolQuery query) {
        callDisplayPoolService.deletePool(query);
        return ApiResponse.success();
    }

    @Log(title = "号码池详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:pool:get')")
    @Operation(summary = "号码池详情", method = "POST")
    @PostMapping("/pool/get/{id}")
    public ApiResponse<CallDisplayPoolVo> getPoolDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(callDisplayPoolService.getPoolDetail(id));
    }

    @Log(title = "号码池列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:display:pool:page:list')")
    @Operation(summary = "号码池列表(分页)", method = "POST")
    @PostMapping("/pool/page/list")
    public ApiResponse<PageInfo<CallDisplayPoolVo>> pagePoolList(@RequestBody CallDisplayPoolQuery query) {
        PageInfo<CallDisplayPoolVo> list = callDisplayPoolService.pagePoolList(query);
        return ApiResponse.success(list);
    }

    @Operation(summary = "号码池列表(不分页)", method = "POST")
    @PostMapping("/pool/list")
    public ApiResponse<List<CallDisplayPoolVo>> getPoolList(@RequestBody CallDisplayPoolQuery query) {
        List<CallDisplayPoolVo> list = callDisplayPoolService.getPoolList(query);
        return ApiResponse.success(list);
    }
}
