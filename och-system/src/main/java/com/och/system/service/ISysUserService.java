package com.och.system.service;

import com.github.pagehelper.PageInfo;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.SysUser;
import com.och.system.domain.feature.IUserVo;
import com.och.system.domain.query.user.SysUserAddQuery;
import com.och.system.domain.query.user.SysUserQuery;
import com.och.system.domain.query.user.SysUserUpdateQuery;
import com.och.system.domain.vo.user.SysSimpleUserVo;
import com.och.system.domain.vo.user.SysUserVo;

import java.util.List;

/**
 * 用户管理服务接口
 * <p>定义系统用户的增删改查、密码修改、角色关联和用户信息装饰等操作契约。
 * 实现类通过 MyBatis-Plus 的 BaseServiceImpl 获得通用 CRUD 能力。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>模板方法</b>：继承 BaseServiceImpl，复用 save/update/delete 等通用模板</li>
 *   <li><b>装饰器</b>：decorate() 方法为 IUserVo 填充创建人/更新人昵称，不改变原有对象结构</li>
 * </ul>
 *
 * @author danmo
 * @since 2024-02-20 18:41:33
 */
public interface ISysUserService extends IBaseService<SysUser> {

    SysUser getByUsername(String username);

    SysUserVo getByUserId(Long userId);

    PageInfo<SysUserVo> getPageList(SysUserQuery query);

    List<SysUserVo> getList(SysUserQuery query);

    void decorate(IUserVo userVo);

    void decorate(List<? extends IUserVo> userList);

    void add(SysUserAddQuery query);

    void edit(SysUserUpdateQuery query);

    void editPassWord(SysUserAddQuery query);

    void delete(SysUserQuery query);

    List<SysSimpleUserVo> getSelectList( SysUserQuery query);
}

