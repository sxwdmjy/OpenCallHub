package com.och.api.controller.sys;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.SysOperLog;
import com.och.system.domain.query.BaseQuery;
import com.och.system.domain.query.log.SysOperationLogQuery;
import com.och.system.service.ISysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author danmo
 * @date 2024-03-13 14:01
 **/

@Tag(name = "日志管理")
@RestController
@RequestMapping("/system/v1/log")
public class SysOperationLogController extends BaseController {

    @Autowired
    private ISysOperLogService iSysOperLogService;

    @Log(title = "日志列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:log:list')")
    @Operation(summary = "日志列表", method = "POST")
    @PostMapping("/list")
    public ResResult<PageInfo<SysOperLog>> list(@RequestBody SysOperationLogQuery query) {
        List<SysOperLog> list = iSysOperLogService.getList(query);
        PageInfo<SysOperLog> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }


    @Log(title = "删除日志", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:log:delete')")
    @Operation(summary = "删除日志", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody SysOperationLogQuery query) {
        iSysOperLogService.delete(query);
        return success();
    }

    @Log(title = "清空日志", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:log:empty')")
    @Operation(summary = "清空日志", method = "POST")
    @PostMapping("/empty")
    public ResResult empty(@RequestBody BaseQuery query) {
        iSysOperLogService.emptyOperation(query);
        return success();
    }

}
