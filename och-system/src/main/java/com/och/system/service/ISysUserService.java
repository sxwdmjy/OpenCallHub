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
 * 用户信息表(User)表服务接口
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

