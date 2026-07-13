package com.och.system.service;

import cn.hutool.core.lang.tree.Tree;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.SysMenu;
import com.och.system.domain.query.menu.SysMenuAddQuery;
import com.och.system.domain.query.menu.SysMenuQuery;

import java.util.List;

/**
 * 菜单权限管理服务接口
 * <p>定义系统菜单（目录/菜单/按钮）的增删改查和树形构建操作。
 * 使用 Hutool 的 TreeUtil 将扁平菜单列表构建为前端所需的层级树结构。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>组合模式</b>：菜单本身是天然的树形结构（目录→菜单→按钮），
 *       buildMenuTree() 方法将扁平列表组合为层级树</li>
 *   <li><b>模板方法</b>：继承 IBaseService，复用通用 CRUD 模板</li>
 * </ul>
 *
 * @author danmo
 * @since 2024-02-22 11:22:25
 */
public interface ISysMenuService extends IBaseService<SysMenu> {

    void add(SysMenuAddQuery query);

    void edit(SysMenuAddQuery query);

    List<SysMenu> getList(SysMenuQuery query);

    void delete(SysMenuQuery query);

    List<Tree<Long>> buildMenuTree(List<SysMenu> list);

    List<SysMenu> getMenuListByRoleIds(List<Long> roleIds);
}

