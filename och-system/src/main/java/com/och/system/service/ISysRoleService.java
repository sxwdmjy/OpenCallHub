package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.SysRole;
import com.och.system.domain.query.role.SysRoleAddQuery;
import com.och.system.domain.query.role.SysRoleQuery;
import com.och.system.domain.vo.role.SysRoleVo;

import java.util.List;

/**
 * 角色管理服务接口
 * <p>定义系统角色的增删改查操作。角色通过 SysRoleMenu 关联菜单权限，
 * 通过 SysUserRole 关联用户，构成 RBAC 权限模型的核心纽带。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>模板方法</b>：继承 IBaseService，复用通用 CRUD 模板</li>
 *   <li><b>组合模式</b>：角色-菜单的树形关联体现了组合模式思想，菜单权限按树形层级组织</li>
 * </ul>
 *
 * @author danmo
 * @since 2024-02-22 11:22:25
 */
public interface ISysRoleService extends IBaseService<SysRole> {

    List<SysRoleVo> getRoleByUserId(Long userId);

    void add(SysRoleAddQuery query);

    void edit(SysRoleAddQuery query);

    SysRoleVo getDetail(SysRoleQuery query);

    void delete(SysRoleQuery query);

    List<SysRoleVo> getList(SysRoleQuery query);

    List<SysRoleVo> getPageList(SysRoleQuery query);

}

