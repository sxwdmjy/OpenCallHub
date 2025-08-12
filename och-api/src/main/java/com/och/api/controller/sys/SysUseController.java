package com.och.api.controller.sys;

import cn.hutool.core.lang.tree.Tree;
import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.common.enums.ExceptionStatusEnum;
import com.och.common.exception.RequestException;
import com.och.security.utils.SecurityUtils;
import com.och.system.domain.entity.SysMenu;
import com.och.system.domain.query.menu.SysMenuQuery;
import com.och.system.domain.query.user.SysUserAddQuery;
import com.och.system.domain.query.user.SysUserQuery;
import com.och.system.domain.query.user.SysUserUpdateQuery;
import com.och.system.domain.vo.user.SysSimpleUserVo;
import com.och.system.domain.vo.user.SysUserVo;
import com.och.system.service.ISysMenuService;
import com.och.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author danmo
 * @date 2024-02-21 17:16
 **/
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/v1/user")
public class SysUseController extends BaseController {

    @Autowired
    private ISysUserService iSysUserService;

    @Autowired
    private ISysMenuService iSysMenuService;

    @Log(title = "新增用户", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:user:add')")
    @Operation(summary = "新增用户", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated SysUserAddQuery query) {
        iSysUserService.add(query);
        return success();
    }

    @Log(title = "修改用户", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:user:edit')")
    @Operation(summary = "修改用户", method = "POST")
    @PostMapping("/edit/{userId}")
    public ResResult edit(@PathVariable("userId") Long userId, @RequestBody @Validated SysUserUpdateQuery query) {
        query.setUserId(userId);
        iSysUserService.edit(query);
        return success();
    }

    @Log(title = "修改用户密码", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:user:editPassWord')")
    @Operation(summary = "修改用户密码", method = "POST", description = "只传密码字段")
    @PostMapping("/editPassWord/{userId}")
    public ResResult editPassWord(@PathVariable("userId") Long userId, @RequestBody SysUserAddQuery query) {
        query.setUserId(userId);
        iSysUserService.editPassWord(query);
        return success();
    }

    @Log(title = "删除用户", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:user:delete')")
    @Operation(summary = "删除用户", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody SysUserQuery query) {
        iSysUserService.delete(query);
        return success();
    }

    @Log(title = "获取用户详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:user:get')")
    @Operation(summary = "获取用户详情", method = "POST")
    @PostMapping("/get")
    public ResResult<SysUserVo> get(@RequestBody SysUserQuery query) {
        if (Objects.isNull(query.getUserId())) {
            throw new RequestException(ExceptionStatusEnum.ERROR_USERID_NOT_NULL.getCode(), ExceptionStatusEnum.ERROR_USERID_NOT_NULL.getMsg());
        }
        SysUserVo sysUserVo = iSysUserService.getByUserId(query.getUserId());
        return success(sysUserVo);
    }

    @Log(title = "获取用户列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:user:list')")
    @Operation(summary = "获取用户列表", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<SysUserVo>> getPageList(@RequestBody SysUserQuery query) {
        PageInfo<SysUserVo> pageInfo = iSysUserService.getPageList(query);
        return success(pageInfo);
    }

    @Log(title = "获取用户下拉列表", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "获取用户下拉列表", method = "POST")
    @PostMapping("/select/list")
    public ResResult<List<SysSimpleUserVo>> getSelectList(@RequestBody SysUserQuery query) {
        return success(iSysUserService.getSelectList(query));
    }

    @Log(title = "获取用户菜单", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "获取用户菜单", method = "GET")
    @GetMapping("getRouters")
    public ResResult<List<Tree<Long>>> getRouters() {
        // 角色信息
        List<SysMenu> menus = new LinkedList<>();
        List<Long> roles = SecurityUtils.getRole();
        if ((!CollectionUtils.isEmpty(roles) && roles.contains(1L)) || SecurityUtils.getUserId() == 1L) {
            SysMenuQuery query = new SysMenuQuery();
            menus.addAll(iSysMenuService.getList(query));
        } else {
            menus.addAll(iSysMenuService.getMenuListByRoleIds(roles));
        }
        List<Tree<Long>> trees = iSysMenuService.buildMenuTree(menus);
        return success(trees);
    }
}
