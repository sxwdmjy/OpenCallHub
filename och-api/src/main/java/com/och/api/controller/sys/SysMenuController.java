package com.och.api.controller.sys;

import cn.hutool.core.lang.tree.Tree;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.common.enums.ExceptionStatusEnum;
import com.och.common.exception.RequestException;
import com.och.system.domain.entity.SysMenu;
import com.och.system.domain.query.menu.SysMenuAddQuery;
import com.och.system.domain.query.menu.SysMenuQuery;
import com.och.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 菜单管理
 *
 * @author danmo
 * @date 2023年07月06日 23:02
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/v1/menu")
public class SysMenuController extends BaseController {

    @Autowired
    private ISysMenuService iSysMenuService;


    @Log(title = "新增菜单",businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:menu:add')")
    @Operation(summary = "新增菜单", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated SysMenuAddQuery query) {
        iSysMenuService.add(query);
        return success();
    }

    @Log(title = "修改菜单",businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:menu:edit')")
    @Operation(summary = "修改菜单", method = "POST")
    @PostMapping("/edit/{menuId}")
    public ResResult edit(@PathVariable("menuId") Long menuId, @RequestBody @Validated SysMenuAddQuery query) {
        query.setMenuId(menuId);
        iSysMenuService.edit(query);
        return success();
    }

    @Log(title = "菜单详情",businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:menu:get')")
    @Operation(summary = "菜单详情", method = "POST")
    @PostMapping("/get/{menuId}")
    public ResResult<SysMenu> get(@RequestBody SysMenuQuery query) {
        if (Objects.isNull(query.getMenuId())) {
            throw new RequestException(ExceptionStatusEnum.ERROR_MENUID_NOT_NULL.getCode(), ExceptionStatusEnum.ERROR_MENUID_NOT_NULL.getMsg());
        }
        return success(iSysMenuService.getById(query.getMenuId()));
    }


    @Log(title = "删除菜单",businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:menu:delete')")
    @Operation(summary = "删除菜单", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody SysMenuQuery query) {
        iSysMenuService.delete(query);
        return success();
    }

    @Log(title = "菜单列表",businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:menu:list')")
    @Operation(summary = "菜单列表", method = "POST")
    @PostMapping("/list")
    public ResResult<List<Tree<Long>>> list(@RequestBody SysMenuQuery query) {
        List<SysMenu> list = iSysMenuService.getList(query);
        List<Tree<Long>> treeList = iSysMenuService.buildMenuTree(list);
        return success(treeList);
    }
}
