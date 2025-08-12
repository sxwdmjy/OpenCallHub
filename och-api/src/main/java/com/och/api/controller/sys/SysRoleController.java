package com.och.api.controller.sys;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.common.enums.ExceptionStatusEnum;
import com.och.common.exception.RequestException;
import com.och.system.domain.query.role.SysRoleAddQuery;
import com.och.system.domain.query.role.SysRoleQuery;
import com.och.system.domain.vo.role.SysRoleVo;
import com.och.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 角色管理
 *
 * @author danmo
 * @date 2024-02-21 15:08
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/v1/role")
public class SysRoleController extends BaseController {

    @Autowired
    private ISysRoleService iSysRoleService;

    @Log(title = "新增角色",businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:role:add')")
    @Operation(summary = "新增角色", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated SysRoleAddQuery query) {
        iSysRoleService.add(query);
        return success();
    }

    @Log(title = "修改角色",businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:role:edit')")
    @Operation(summary = "修改角色", method = "POST")
    @PostMapping("/edit/{roleId}")
    public ResResult edit(@PathVariable("roleId") Long roleId, @RequestBody @Validated SysRoleAddQuery query) {
        query.setRoleId(roleId);
        iSysRoleService.edit(query);
        return success();
    }

    @Log(title = "角色详情",businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:role:get')")
    @Operation(summary = "角色详情", method = "POST")
    @PostMapping("/get")
    public ResResult<SysRoleVo> get(@RequestBody SysRoleQuery query) {
        if (Objects.isNull(query.getRoleId())) {
            throw new RequestException(ExceptionStatusEnum.ERROR_ROLEID_NOT_NULL.getCode(), ExceptionStatusEnum.ERROR_ROLEID_NOT_NULL.getMsg());
        }
        return success(iSysRoleService.getDetail(query));
    }

    @Log(title = "删除角色",businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:role:delete')")
    @Operation(summary = "删除角色", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody SysRoleQuery query) {
        iSysRoleService.delete(query);
        return success();
    }

    @Log(title = "角色列表",businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:role:list')")
    @Operation(summary = "角色列表", method = "POST")
    @PostMapping("/list")
    public ResResult<PageInfo<SysRoleVo>> list(@RequestBody SysRoleQuery query) {
        List<SysRoleVo> list = iSysRoleService.getPageList(query);
        PageInfo<SysRoleVo> sysRolePageInfo = new PageInfo<>(list);
        return success(sysRolePageInfo);
    }
}
