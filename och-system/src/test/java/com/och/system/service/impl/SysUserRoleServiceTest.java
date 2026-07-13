package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.och.system.domain.entity.SysUserRole;
import com.och.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysUserRoleServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class SysUserRoleServiceTest {

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Spy
    @InjectMocks
    private SysUserRoleServiceImpl sysUserRoleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sysUserRoleService, "baseMapper", sysUserRoleMapper);
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysUserRole.class);
    }

    // ── updateBatch: remove all roles ─────────────────────────────────────

    @Test
    void updateBatch_withEmptyRoleIds_shouldRemoveAllRoles() {
        Long userId = 1L;

        doReturn(true).when(sysUserRoleService).update(any(LambdaUpdateWrapper.class));

        sysUserRoleService.updateBatch(userId, Collections.emptyList());

        verify(sysUserRoleService).update(any(LambdaUpdateWrapper.class));
        verify(sysUserRoleService, never()).saveBatch(anyList());
    }

    // ── updateBatch: add all new roles ───────────────────────────────────

    @Test
    void updateBatch_withNoExistingRoles_shouldAddAllNewRoles() {
        Long userId = 1L;
        List<Long> roleIds = List.of(1L, 2L, 3L);

        doReturn(Collections.emptyList()).when(sysUserRoleService).list(any(LambdaQueryWrapper.class));
        doReturn(true).when(sysUserRoleService).saveBatch(anyList());

        sysUserRoleService.updateBatch(userId, roleIds);

        verify(sysUserRoleService).list(any(LambdaQueryWrapper.class));
        verify(sysUserRoleService).saveBatch(anyList());
    }

    // ── updateBatch: add and remove roles ────────────────────────────────

    @Test
    void updateBatch_withMixedRoles_shouldRemoveOldAndAddNew() {
        Long userId = 1L;
        // current roles: 1, 2; new roles: 2, 3 → remove 1, keep 2, add 3
        List<Long> newRoleIds = List.of(2L, 3L);

        SysUserRole existingRole1 = SysUserRole.builder().id(10L).userId(userId).roleId(1L).delFlag(0).build();
        SysUserRole existingRole2 = SysUserRole.builder().id(11L).userId(userId).roleId(2L).delFlag(0).build();

        doReturn(List.of(existingRole1, existingRole2))
                .when(sysUserRoleService).list(any(LambdaQueryWrapper.class));
        doReturn(true).when(sysUserRoleService).update(any(LambdaUpdateWrapper.class));
        doReturn(true).when(sysUserRoleService).saveBatch(anyList(), anyInt());

        sysUserRoleService.updateBatch(userId, newRoleIds);

        verify(sysUserRoleService).list(any(LambdaQueryWrapper.class));
        verify(sysUserRoleService).update(any(LambdaUpdateWrapper.class));
        verify(sysUserRoleService).saveBatch(anyList(), eq(200));
    }

    // ── updateBatch: all roles remain same ────────────────────────────────

    @Test
    void updateBatch_withAllSameRoles_shouldNotAddOrRemove() {
        Long userId = 1L;
        List<Long> newRoleIds = List.of(1L, 2L);

        SysUserRole existingRole1 = SysUserRole.builder().id(10L).userId(userId).roleId(1L).delFlag(0).build();
        SysUserRole existingRole2 = SysUserRole.builder().id(11L).userId(userId).roleId(2L).delFlag(0).build();

        doReturn(List.of(existingRole1, existingRole2))
                .when(sysUserRoleService).list(any(LambdaQueryWrapper.class));

        sysUserRoleService.updateBatch(userId, newRoleIds);

        verify(sysUserRoleService).list(any(LambdaQueryWrapper.class));
        verify(sysUserRoleMapper, never()).update(any(), any());
        verify(sysUserRoleService, never()).saveBatch(anyList(), anyInt());
    }
}
