package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.och.system.domain.entity.SysRoleMenu;
import com.och.system.mapper.SysRoleMenuMapper;
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
 * SysRoleMenuServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class SysRoleMenuServiceTest {

    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Spy
    @InjectMocks
    private SysRoleMenuServiceImpl sysRoleMenuService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sysRoleMenuService, "baseMapper", sysRoleMenuMapper);
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysRoleMenu.class);
    }

    // ── updateBatch: remove all menus ─────────────────────────────────────

    @Test
    void updateBatch_withEmptyMenuIds_shouldRemoveAllMenus() {
        Long roleId = 1L;

        doReturn(true).when(sysRoleMenuService).update(any(LambdaUpdateWrapper.class));

        sysRoleMenuService.updateBatch(roleId, Collections.emptyList());

        verify(sysRoleMenuService).update(any(LambdaUpdateWrapper.class));
        verify(sysRoleMenuService, never()).saveBatch(anyList());
    }

    // ── updateBatch: add all new menus ───────────────────────────────────

    @Test
    void updateBatch_withNoExistingMenus_shouldAddAllNewMenus() {
        Long roleId = 1L;
        List<Long> menuIds = List.of(1L, 2L, 3L);

        doReturn(Collections.emptyList()).when(sysRoleMenuService).list(any(LambdaQueryWrapper.class));
        doReturn(true).when(sysRoleMenuService).saveBatch(anyList());

        sysRoleMenuService.updateBatch(roleId, menuIds);

        verify(sysRoleMenuService).list(any(LambdaQueryWrapper.class));
        verify(sysRoleMenuService).saveBatch(anyList());
    }

    // ── updateBatch: add and remove menus ────────────────────────────────

    @Test
    void updateBatch_withMixedMenus_shouldRemoveOldAndAddNew() {
        Long roleId = 1L;
        // current menus: 1, 2; new menus: 2, 3 → remove 1, keep 2, add 3
        List<Long> newMenuIds = List.of(2L, 3L);

        SysRoleMenu existingMenu1 = SysRoleMenu.builder().id(10L).roleId(roleId).menuId(1L).delFlag(0).build();
        SysRoleMenu existingMenu2 = SysRoleMenu.builder().id(11L).roleId(roleId).menuId(2L).delFlag(0).build();

        doReturn(List.of(existingMenu1, existingMenu2))
                .when(sysRoleMenuService).list(any(LambdaQueryWrapper.class));
        doReturn(true).when(sysRoleMenuService).update(any(LambdaUpdateWrapper.class));
        doReturn(true).when(sysRoleMenuService).saveBatch(anyList(), anyInt());

        sysRoleMenuService.updateBatch(roleId, newMenuIds);

        verify(sysRoleMenuService).list(any(LambdaQueryWrapper.class));
        verify(sysRoleMenuService).update(any(LambdaUpdateWrapper.class));
        verify(sysRoleMenuService).saveBatch(anyList(), eq(200));
    }

    // ── updateBatch: all menus remain same ────────────────────────────────

    @Test
    void updateBatch_withAllSameMenus_shouldNotAddOrRemove() {
        Long roleId = 1L;
        List<Long> newMenuIds = List.of(1L, 2L);

        SysRoleMenu existingMenu1 = SysRoleMenu.builder().id(10L).roleId(roleId).menuId(1L).delFlag(0).build();
        SysRoleMenu existingMenu2 = SysRoleMenu.builder().id(11L).roleId(roleId).menuId(2L).delFlag(0).build();

        doReturn(List.of(existingMenu1, existingMenu2))
                .when(sysRoleMenuService).list(any(LambdaQueryWrapper.class));

        sysRoleMenuService.updateBatch(roleId, newMenuIds);

        verify(sysRoleMenuService).list(any(LambdaQueryWrapper.class));
        verify(sysRoleMenuMapper, never()).update(any(), any());
        verify(sysRoleMenuService, never()).saveBatch(anyList(), anyInt());
    }
}
