package com.och.api.controller;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.query.schedule.CallScheduleAddQuery;
import com.och.system.domain.query.schedule.CallScheduleQuery;
import com.och.system.service.ICallScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2024-08-09 17:15
 **/
@Tag(name = "日程管理")
@RestController
@RequestMapping("/call/v1/schedule")
public class CallScheduleController extends BaseController {

    @Autowired
    private ICallScheduleService iCallScheduleService;



    @Log(title = "新增日程", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:schedule:add')")
    @Operation(summary = "新增日程", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CallScheduleAddQuery query) {
        iCallScheduleService.add(query);
        return success();
    }

    @Log(title = "修改日程", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:schedule:edit')")
    @Operation(summary = "修改日程", method = "PUT")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CallScheduleAddQuery query) {
        query.setId(id);
        iCallScheduleService.update(query);
        return success();
    }

    @Log(title = "删除日程", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:schedule:delete')")
    @Operation(summary = "删除日程", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CallScheduleQuery query) {
        iCallScheduleService.delete(query);
        return success();
    }

    @Log(title = "日程详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:schedule:get')")
    @Operation(summary = "日程详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CallSchedule> getDetail(@PathVariable("id") Long id) {
        CallSchedule detail = iCallScheduleService.getDetail(id);
        return success(detail);
    }

    @Log(title = "日程列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:schedule:page:list')")
    @Operation(summary = "日程列表(分页)", method = "POST")
    @PostMapping("/page/getList")
    public ResResult<PageInfo<CallSchedule>> getPageList(@RequestBody CallScheduleQuery query) {
        List<CallSchedule> list = iCallScheduleService.getPageList(query);
        PageInfo<CallSchedule> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

    @Operation(summary = "日程列表", method = "POST")
    @PostMapping("/getList")
    public ResResult<List<CallSchedule>> getList(@RequestBody CallScheduleQuery query) {
        List<CallSchedule> list = iCallScheduleService.getList(query);
        return success(list);
    }
}
