package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.SysRoleMenu;
import com.och.system.mapper.SysRoleMenuMapper;
import com.och.system.service.ISysRoleMenuService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色菜单关联表(SysRoleMenu)表服务实现类
 *
 * @author danmo
 * @since 2024-02-22 11:22:25
 */
@Service
public class SysRoleMenuServiceImpl extends BaseServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements ISysRoleMenuService {

    @Override
    public void updateBatch(Long roleId, List<Long> menuIds) {
        //菜单ID为空全部删除
        if (CollectionUtil.isEmpty(menuIds)) {
            update(new LambdaUpdateWrapper<SysRoleMenu>().set(SysRoleMenu::getDelFlag, 0).eq(SysRoleMenu::getRoleId, roleId));
            return;
        }
        List<SysRoleMenu> sysRoleMenus = list(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId).eq(SysRoleMenu::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        //菜单为空全部新增
        if (CollectionUtil.isEmpty(sysRoleMenus)) {
            List<SysRoleMenu> sysRoleMenuList = menuIds.stream().map(menuId -> {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setRoleId(roleId);
                sysRoleMenu.setMenuId(menuId);
                return sysRoleMenu;
            }).collect(Collectors.toList());
            saveBatch(sysRoleMenuList);
        } else {
            //需要删除菜单
            List<Long> delIdList = sysRoleMenus.stream().filter(fsRoleMenu -> !menuIds.contains(fsRoleMenu.getMenuId())).map(SysRoleMenu::getId).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(delIdList)){
                update(new LambdaUpdateWrapper<SysRoleMenu>().set(SysRoleMenu::getDelFlag, DeleteStatusEnum.DELETE_YES.getIndex()).in(SysRoleMenu::getId, delIdList));
            }

            //需要新增菜单
            List<Long> addMenuIds = menuIds.stream().filter(menuId -> sysRoleMenus.stream().noneMatch(fsRoleMenu -> Objects.equals(fsRoleMenu.getMenuId(), menuId))).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(addMenuIds)){
                List<SysRoleMenu> addRoleMenuList = addMenuIds.stream().map(menuId -> {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(roleId);
                    sysRoleMenu.setMenuId(menuId);
                    return sysRoleMenu;
                }).collect(Collectors.toList());
                saveBatch(addRoleMenuList, 200);
            }

        }
    }
}

