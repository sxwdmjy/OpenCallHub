package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.CallEngine;
import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.query.engine.CallEngineAddQuery;
import com.och.system.domain.query.engine.CallEngineQuery;
import com.och.system.domain.query.route.CallRouteAddQuery;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.query.schedule.CallScheduleAddQuery;
import com.och.system.domain.query.schedule.CallScheduleQuery;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.service.ICallEngineService;
import com.och.system.service.ICallRouteService;
import com.och.system.service.ICallScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 呼叫设置管理（路由、引擎、日程）
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "呼叫设置管理")
@RestController
@RequestMapping("/call/v1/setup")
@RequiredArgsConstructor
public class CallSetupController {

    private final ICallRouteService callRouteService;
    private final ICallEngineService callEngineService;
    private final ICallScheduleService callScheduleService;

    // ==================== 号码路由 ====================

    @Log(title = "新增号码路由", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:rout:add')")
    @Operation(summary = "新增号码路由", method = "POST")
    @PostMapping("/route/add")
    public ApiResponse<Void> addRoute(@RequestBody @Validated CallRouteAddQuery query) {
        callRouteService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改号码路由", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:rout:edit')")
    @Operation(summary = "修改号码路由", method = "POST")
    @PostMapping("/route/edit/{id}")
    public ApiResponse<Void> editRoute(@PathVariable("id") Long id, @RequestBody @Validated CallRouteAddQuery query) {
        query.setId(id);
        callRouteService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "启用路由", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:rout:enable')")
    @Operation(summary = "启用路由", method = "POST")
    @PostMapping("/route/enable/{id}")
    public ApiResponse<Void> enableRoute(@PathVariable("id") Long id) {
        callRouteService.enable(id);
        return ApiResponse.success();
    }

    @Log(title = "禁用路由", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:rout:disable')")
    @Operation(summary = "禁用路由", method = "POST")
    @PostMapping("/route/disable/{id}")
    public ApiResponse<Void> disableRoute(@PathVariable("id") Long id) {
        callRouteService.disable(id);
        return ApiResponse.success();
    }

    @Log(title = "删除号码路由", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:rout:delete')")
    @Operation(summary = "删除号码路由", method = "POST")
    @PostMapping("/route/delete")
    public ApiResponse<Void> deleteRoute(@RequestBody CallRouteQuery query) {
        callRouteService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "号码路由详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:rout:get')")
    @Operation(summary = "号码路由详情", method = "POST")
    @PostMapping("/route/get/{id}")
    public ApiResponse<CallRouteVo> getRoute(@PathVariable("id") Long id) {
        return ApiResponse.success(callRouteService.getDetail(id));
    }

    @Log(title = "号码路由列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:rout:page:list')")
    @Operation(summary = "号码路由列表", method = "POST")
    @PostMapping("/route/page/list")
    public ApiResponse<PageInfo<CallRouteVo>> pageRouteList(@RequestBody CallRouteQuery query) {
        List<CallRouteVo> list = callRouteService.getPageList(query);
        PageInfo<CallRouteVo> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    // ==================== 引擎管理 ====================

    @Log(title = "新增引擎", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:engine:add')")
    @Operation(summary = "新增引擎", method = "POST")
    @PostMapping("/engine/add")
    public ApiResponse<Void> addEngine(@RequestBody @Validated CallEngineAddQuery query) {
        callEngineService.addEngine(query);
        return ApiResponse.success();
    }

    @Log(title = "修改引擎", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:engine:edit')")
    @Operation(summary = "修改引擎", method = "POST")
    @PostMapping("/engine/edit/{id}")
    public ApiResponse<Void> editEngine(@PathVariable("id") Long id, @RequestBody @Validated CallEngineAddQuery query) {
        query.setId(id);
        callEngineService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "删除引擎", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:engine:delete')")
    @Operation(summary = "删除引擎", method = "POST")
    @PostMapping("/engine/delete")
    public ApiResponse<Void> deleteEngine(@RequestBody CallEngineQuery query) {
        callEngineService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "引擎详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:engine:get')")
    @Operation(summary = "引擎详情", method = "POST")
    @PostMapping("/engine/get/{id}")
    public ApiResponse<CallEngine> getEngineDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(callEngineService.getDetail(id));
    }

    @Log(title = "引擎列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:engine:page:list')")
    @Operation(summary = "引擎列表(分页)", method = "POST")
    @PostMapping("/engine/page/list")
    public ApiResponse<PageInfo<CallEngine>> pageEngineList(@RequestBody CallEngineQuery query) {
        List<CallEngine> pageList = callEngineService.getPageList(query);
        PageInfo<CallEngine> pageInfo = new PageInfo<>(pageList);
        return ApiResponse.success(pageInfo);
    }

    @Operation(summary = "引擎列表", method = "POST")
    @PostMapping("/engine/list")
    public ApiResponse<List<CallEngine>> getEngineList(@RequestBody CallEngineQuery query) {
        List<CallEngine> list = callEngineService.getList(query);
        return ApiResponse.success(list);
    }

    // ==================== 日程管理 ====================

    @Log(title = "新增日程", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:schedule:add')")
    @Operation(summary = "新增日程", method = "POST")
    @PostMapping("/schedule/add")
    public ApiResponse<Void> addSchedule(@RequestBody @Validated CallScheduleAddQuery query) {
        callScheduleService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改日程", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:schedule:edit')")
    @Operation(summary = "修改日程", method = "PUT")
    @PostMapping("/schedule/edit/{id}")
    public ApiResponse<Void> editSchedule(@PathVariable("id") Long id, @RequestBody @Validated CallScheduleAddQuery query) {
        query.setId(id);
        callScheduleService.update(query);
        return ApiResponse.success();
    }

    @Log(title = "删除日程", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:schedule:delete')")
    @Operation(summary = "删除日程", method = "POST")
    @PostMapping("/schedule/delete")
    public ApiResponse<Void> deleteSchedule(@RequestBody CallScheduleQuery query) {
        callScheduleService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "日程详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:schedule:get')")
    @Operation(summary = "日程详情", method = "POST")
    @PostMapping("/schedule/get/{id}")
    public ApiResponse<CallSchedule> getScheduleDetail(@PathVariable("id") Long id) {
        CallSchedule detail = callScheduleService.getDetail(id);
        return ApiResponse.success(detail);
    }

    @Log(title = "日程列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:schedule:page:list')")
    @Operation(summary = "日程列表(分页)", method = "POST")
    @PostMapping("/schedule/page/getList")
    public ApiResponse<PageInfo<CallSchedule>> pageScheduleList(@RequestBody CallScheduleQuery query) {
        List<CallSchedule> list = callScheduleService.getPageList(query);
        PageInfo<CallSchedule> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    @Operation(summary = "日程列表", method = "POST")
    @PostMapping("/schedule/getList")
    public ApiResponse<List<CallSchedule>> getScheduleList(@RequestBody CallScheduleQuery query) {
        List<CallSchedule> list = callScheduleService.getList(query);
        return ApiResponse.success(list);
    }
}
