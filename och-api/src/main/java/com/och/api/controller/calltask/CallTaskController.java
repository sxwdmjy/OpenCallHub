package com.och.api.controller.calltask;


import com.github.pagehelper.PageInfo;
import com.och.calltask.domain.query.CallTaskAddQuery;
import com.och.calltask.domain.query.CallTaskContactImportQuery;
import com.och.calltask.domain.query.CallTaskContactQuery;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskContactVo;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.calltask.service.ICallTaskService;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 呼叫任务管理
 *
 * @author danmo
 * @date 2025/06/19 09:53
 */
@Tag(name = "呼叫任务管理")
@RestController
@RequestMapping("/call/task/v1")
public class CallTaskController extends BaseController {

    @Autowired
    private ICallTaskService callTaskService;

    @Log(title = "新增呼叫任务", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:task:add')")
    @Operation(summary = "新增呼叫任务", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CallTaskAddQuery query) {
        callTaskService.add(query);
        return success();
    }

    @Log(title = "修改呼叫任务", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:task:edit')")
    @Operation(summary = "修改呼叫任务", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CallTaskAddQuery query) {
        query.setId(id);
        callTaskService.edit(query);
        return success();
    }

    @Log(title = "删除呼叫任务", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:task:delete')")
    @Operation(summary = "删除呼叫任务", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CallTaskQuery query) {
        callTaskService.detele(query);
        return success();
    }

    @Log(title = "呼叫任务详情", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "呼叫任务详情", method = "POST")
    @PreAuthorize("@authz.hasPerm('call:task:get')")
    @PostMapping("/get/{id}")
    public ResResult<CallTaskVo> get(@PathVariable("id") Long id) {
        return success(callTaskService.getDetail(id));
    }

    @Log(title = "呼叫任务列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:page:list')")
    @Operation(summary = "呼叫任务列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CallTaskVo>> pageList(@RequestBody CallTaskQuery query) {
        List<CallTaskVo> list = callTaskService.pageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "呼叫任务列表", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "呼叫任务列表", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CallTaskVo>> list(@RequestBody CallTaskQuery query) {
        List<CallTaskVo> list = callTaskService.getList(query);
        return success(list);
    }

    @Log(title = "开始任务", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:task:start')")
    @Operation(summary = "开始任务", method = "POST")
    @PostMapping("/start/{id}")
    public ResResult start(@PathVariable("id") Long id) {
        callTaskService.startTask(id);
        return success();
    }

    @Log(title = "暂停任务", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:task:pause')")
    @Operation(summary = "暂停任务", method = "POST")
    @PostMapping("/pause/{id}")
    public ResResult pause(@PathVariable("id") Long id) {
        callTaskService.pauseTask(id);
        return success();
    }

    @Log(title = "结束任务", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:task:end')")
    @Operation(summary = "结束任务", method = "POST")
    @PostMapping("/end/{id}")
    public ResResult end(@PathVariable("id") Long id) {
        callTaskService.endTask(id);
        return success();
    }

    @Log(title = "任务联系人列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:contact:list')")
    @Operation(summary = "任务联系人列表(分页)", method = "POST")
    @PostMapping("/task/customer/list")
    public ResResult<PageInfo<CallTaskContactVo>> taskContactList(@RequestBody CallTaskContactQuery query) {
        List<CallTaskContactVo> list = callTaskService.getTaskContactPageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "导入任务联系人", businessType = BusinessTypeEnum.IMPORT)
    @PreAuthorize("@authz.hasPerm('call:task:contact:import')")
    @Operation(summary = "导入任务联系人", method = "POST")
    @PostMapping("/task/customer/import")
    public ResResult importTaskContact(@RequestBody CallTaskContactImportQuery query, @RequestParam("file") MultipartFile file) {
        callTaskService.importTaskContact(query,file);
        return success();
    }


}
