package com.och.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.SysMenu;
import com.och.system.domain.query.menu.SysMenuAddQuery;
import com.och.system.domain.query.menu.SysMenuQuery;
import com.och.system.mapper.SysMenuMapper;
import com.och.system.service.ISysMenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 菜单权限表(SysMenu)表服务实现类
 *
 * @author danmo
 * @since 2024-02-22 11:22:25
 */
@Service
public class SysMenuServiceImpl extends BaseServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public void add(SysMenuAddQuery query) {
        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(query, sysMenu);
        save(sysMenu);
    }

    @Override
    public void edit(SysMenuAddQuery query) {
        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(query, sysMenu);
        sysMenu.setMenuId(query.getMenuId());
        updateById(sysMenu);
    }

    @Override
    public List<SysMenu> getList(SysMenuQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public void delete(SysMenuQuery query) {
        Optional.ofNullable(query.getMenuIds()).orElseGet(ArrayList::new).add(query.getMenuId());
        if (!CollectionUtils.isEmpty(query.getMenuIds())) {
            update(new LambdaUpdateWrapper<SysMenu>().set(SysMenu::getDelFlag, DeleteStatusEnum.DELETE_YES.getIndex())
                    .and(item -> item.in(SysMenu::getMenuId, query.getMenuIds())).or().in(SysMenu::getParentId,query.getMenuIds()));
        }
    }

    @Override
    public List<Tree<Long>> buildMenuTree(List<SysMenu> list) {
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        //自定义属性名 都要默认值的
        treeNodeConfig.setWeightKey("orderNum");
        treeNodeConfig.setIdKey("menuId");
        treeNodeConfig.setParentIdKey("parentId");
        return TreeUtil.build(list, 0L, treeNodeConfig, ((sysMenu, treeNode) -> {
            treeNode.setId(sysMenu.getMenuId());//id
            treeNode.setParentId(sysMenu.getParentId());//父id
            treeNode.putExtra("name", sysMenu.getMenuName());
            treeNode.putExtra("orderNum", sysMenu.getOrderNum());
            treeNode.putExtra("menuType", sysMenu.getMenuType());
            treeNode.putExtra("component", sysMenu.getComponent());
            treeNode.putExtra("icon", sysMenu.getIcon());
            treeNode.putExtra("isFrame", sysMenu.getIsFrame());
            treeNode.putExtra("perms", sysMenu.getPerms());
            treeNode.putExtra("visible", sysMenu.getVisible());
            treeNode.putExtra("path", sysMenu.getPath());
            treeNode.putExtra("status", sysMenu.getStatus());
            treeNode.putExtra("remark", sysMenu.getRemark());
        }));
    }

    @Override
    public List<SysMenu> getMenuListByRoleIds(List<Long> roleIds) {
        return this.baseMapper.getMenuListByRoleIds(roleIds);
    }
}

