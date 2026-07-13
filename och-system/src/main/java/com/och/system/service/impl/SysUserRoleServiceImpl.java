package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.system.domain.entity.SysUserRole;
import com.och.system.mapper.SysUserRoleMapper;
import com.och.system.service.ISysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户角色关联表(SysUserRole)表服务实现类
 *
 * @author danmo
 * @since 2024-02-22 11:22:25
 */
@Service
public class SysUserRoleServiceImpl extends BaseServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {
    @Override
    public void updateBatch(Long userId, List<Long> roleIds) {
        //角色ID为空全部删除
        if (CollectionUtils.isEmpty(roleIds)) {
            update(new LambdaUpdateWrapper<SysUserRole>().set(SysUserRole::getDelFlag, 0).eq(SysUserRole::getUserId, userId));
            return;
        }
        List<SysUserRole> sysUserRoles = list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId).eq(SysUserRole::getDelFlag, 0));
        //角色为空全部新增
        if (CollectionUtils.isEmpty(sysUserRoles)) {
            List<SysUserRole> sysUserRoleList = roleIds.stream().map(roleId -> {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(userId);
                sysUserRole.setRoleId(roleId);
                return sysUserRole;
            }).collect(Collectors.toList());
            saveBatch(sysUserRoleList);
        } else {
            //需要删除角色
            List<Long> delIdList = sysUserRoles.stream().filter(sysUserRole -> !roleIds.contains(sysUserRole.getRoleId())).map(SysUserRole::getId).collect(Collectors.toList());
            if (!delIdList.isEmpty()) {
                update(new LambdaUpdateWrapper<SysUserRole>().set(SysUserRole::getDelFlag, 1).in(SysUserRole::getId, delIdList));
            }
            //需要新增角色
            List<Long> addRoleIds = roleIds.stream().filter(roleId -> sysUserRoles.stream().noneMatch(sysUserRole -> Objects.equals(sysUserRole.getRoleId(), roleId))).collect(Collectors.toList());
            if (!addRoleIds.isEmpty()) {
                List<SysUserRole> addUserRoleList = addRoleIds.stream().map(roleId -> {
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setUserId(userId);
                    sysUserRole.setRoleId(roleId);
                    return sysUserRole;
                }).collect(Collectors.toList());
                saveBatch(addUserRoleList, 200);
            }
        }
    }
}

