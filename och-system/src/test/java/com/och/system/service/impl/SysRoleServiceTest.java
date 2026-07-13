package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.system.domain.entity.SysRole;
import com.och.system.domain.entity.SysRoleMenu;
import com.och.system.domain.query.role.SysRoleAddQuery;
import com.och.system.domain.query.role.SysRoleQuery;
import com.och.system.domain.vo.role.SysRoleVo;
import com.och.system.mapper.SysRoleMapper;
import com.och.system.service.ISysRoleMenuService;
import com.och.system.service.ISysUserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * SysRoleServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class SysRoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private ISysUserRoleService userRoleService;

    @Mock
    private ISysRoleMenuService roleMenuService;

    @InjectMocks
    private SysRoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
    }

    // ── addRole ───────────────────────────────────────────────────────────

    @Test
    void addRole_shouldSaveRoleAndAssociateMenus_whenMenuIdsProvided() {
        SysRoleAddQuery query = new SysRoleAddQuery();
        query.setRoleName("admin");
        query.setRoleKey("admin");
        query.setRoleSort(1);
        query.setDataScope(1);
        query.setMenuIds(List.of(1L, 2L));

        when(roleMapper.insert(any(SysRole.class))).thenReturn(1);

        roleService.add(query);

        verify(roleMapper).insert(any(SysRole.class));
        verify(roleMenuService).saveBatch(anyList());
    }

    @Test
    void addRole_shouldSaveRoleWithoutMenus_whenNoMenuIds() {
        SysRoleAddQuery query = new SysRoleAddQuery();
        query.setRoleName("viewer");
        query.setRoleKey("viewer");
        query.setRoleSort(2);

        when(roleMapper.insert(any(SysRole.class))).thenReturn(1);

        roleService.add(query);

        verify(roleMapper).insert(any(SysRole.class));
        verify(roleMenuService, never()).saveBatch(anyList());
    }

    // ── getRole ───────────────────────────────────────────────────────────

    @Test
    void getDetail_shouldReturnRoleVo_whenRoleExists() {
        SysRoleQuery query = new SysRoleQuery();
        query.setRoleId(1L);

        SysRoleVo vo = new SysRoleVo();
        vo.setRoleId(1L);
        vo.setRoleName("admin");
        vo.setRoleKey("admin");

        when(roleMapper.getDetail(query)).thenReturn(vo);

        SysRoleVo result = roleService.getDetail(query);

        assertThat(result).isNotNull();
        assertThat(result.getRoleId()).isEqualTo(1L);
        assertThat(result.getRoleName()).isEqualTo("admin");
        verify(roleMapper).getDetail(query);
    }

    // ── listRoles ─────────────────────────────────────────────────────────

    @Test
    void getRoleByUserId_shouldReturnRolesForUser() {
        Long userId = 1L;
        SysRoleVo vo1 = new SysRoleVo();
        vo1.setRoleId(1L);
        vo1.setRoleName("admin");
        SysRoleVo vo2 = new SysRoleVo();
        vo2.setRoleId(2L);
        vo2.setRoleName("user");

        when(roleMapper.getRoleByUserId(userId)).thenReturn(List.of(vo1, vo2));

        List<SysRoleVo> result = roleService.getRoleByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SysRoleVo::getRoleName).containsExactly("admin", "user");
        verify(roleMapper).getRoleByUserId(userId);
    }

    // ── assignMenus (edit) ────────────────────────────────────────────────

    @Test
    void edit_shouldUpdateRoleAndMenus_whenMenuIdsProvided() {
        SysRoleAddQuery query = new SysRoleAddQuery();
        query.setRoleId(1L);
        query.setRoleName("admin-updated");
        query.setMenuIds(List.of(3L, 4L));

        when(roleMapper.updateById(any(SysRole.class))).thenReturn(1);

        roleService.edit(query);

        verify(roleMapper).updateById(any(SysRole.class));
        verify(roleMenuService).updateBatch(eq(1L), anyList());
    }
}
