package com.och.system.service.impl;

import com.och.common.error.BizException;
import com.och.system.domain.entity.SysUser;
import com.och.system.domain.query.user.SysUserAddQuery;
import com.och.system.domain.query.user.SysUserQuery;
import com.och.system.domain.query.user.SysUserUpdateQuery;
import com.och.system.domain.vo.user.SysSimpleUserVo;
import com.och.system.domain.vo.user.SysUserVo;
import com.och.system.mapper.SysUserMapper;
import com.och.system.service.ISysRoleService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysUserServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private ISysRoleService roleService;

    @Mock
    private ISysUserRoleService userRoleService;

    @InjectMocks
    private SysUserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl.baseMapper is normally @Autowired field-injected
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    // ── addUser ──────────────────────────────────────────────────────────

    @Test
    void addUser_shouldSaveUserAndAssociateRoles_whenUsernameNotDuplicate() {
        SysUserAddQuery query = new SysUserAddQuery();
        query.setUserName("testuser");
        query.setPassword("pwd123");
        query.setNickName("Test User");
        query.setRoleIds(List.of(1L, 2L));

        // ServiceImpl.getOne(wrapper, boolean) calls baseMapper.selectOne(wrapper, boolean)
        doReturn(null).when(userMapper).selectOne(any(), anyBoolean());
        // save calls baseMapper.insert
        doReturn(1).when(userMapper).insert(any(SysUser.class));

        userService.add(query);

        verify(userMapper).insert(any(SysUser.class));
        verify(userRoleService).saveBatch(anyList());
    }

    @Test
    void addUser_shouldThrowBizException_whenUsernameDuplicate() {
        SysUserAddQuery query = new SysUserAddQuery();
        query.setUserName("existing");
        query.setPassword("pwd123");

        SysUser existingUser = SysUser.builder().userId(100L).userName("existing").delFlag(0).build();
        doReturn(existingUser).when(userMapper).selectOne(any(), anyBoolean());

        assertThatThrownBy(() -> userService.add(query))
                .isInstanceOf(BizException.class);

        verify(userMapper, never()).insert(any());
    }

    // ── getUser / getByUsername ──────────────────────────────────────────

    @Test
    void getByUserId_shouldReturnUserVo_whenUserExists() {
        Long userId = 1L;
        SysUserVo vo = new SysUserVo();
        vo.setUserId(userId);
        vo.setUserName("testuser");

        when(userMapper.getByUserId(userId)).thenReturn(vo);

        SysUserVo result = userService.getByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUserName()).isEqualTo("testuser");
        verify(userMapper).getByUserId(userId);
    }

    @Test
    void getByUsername_shouldReturnUser_whenUserExists() {
        String username = "john";
        SysUser user = SysUser.builder().userId(1L).userName(username).build();

        doReturn(user).when(userMapper).selectOne(any(), anyBoolean());

        SysUser result = userService.getByUsername(username);

        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo(username);
    }

    @Test
    void getByUsername_shouldReturnNull_whenUserNotFound() {
        doReturn(null).when(userMapper).selectOne(any(), anyBoolean());

        SysUser result = userService.getByUsername("nonexistent");

        assertThat(result).isNull();
    }

    // ── listUsers ─────────────────────────────────────────────────────────

    @Test
    void getList_shouldReturnUserVoList() {
        SysUserQuery query = new SysUserQuery();
        SysUserVo vo = new SysUserVo();
        vo.setUserId(1L);
        vo.setUserName("user1");

        when(userMapper.getList(query)).thenReturn(List.of(vo));

        List<SysUserVo> result = userService.getList(query);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("user1");
        verify(userMapper).getList(query);
    }

    @Test
    void getSelectList_shouldReturnSimpleUserList() {
        SysUserQuery query = new SysUserQuery();
        SysSimpleUserVo vo = new SysSimpleUserVo();
        vo.setUserId(1L);
        vo.setNickName("Alice");

        when(userMapper.getSelectList(query)).thenReturn(List.of(vo));

        List<SysSimpleUserVo> result = userService.getSelectList(query);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickName()).isEqualTo("Alice");
        verify(userMapper).getSelectList(query);
    }

    // ── updateUser ────────────────────────────────────────────────────────

    @Test
    void editUser_shouldUpdateUser_whenUserExists() {
        Long userId = 1L;
        SysUser existingUser = SysUser.builder().userId(userId).userName("oldname").build();

        when(userMapper.selectById(userId)).thenReturn(existingUser);
        doReturn(1).when(userMapper).updateById(any(SysUser.class));

        SysUserUpdateQuery query = new SysUserUpdateQuery();
        query.setUserId(userId);
        query.setUserName("oldname"); // same username, no duplicate check triggered
        query.setNickName("New Nick");

        userService.edit(query);

        verify(userMapper).selectById(userId);
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void editUser_shouldThrowBizException_whenUserNotFound() {
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        SysUserUpdateQuery query = new SysUserUpdateQuery();
        query.setUserId(userId);
        query.setUserName("newname");

        assertThatThrownBy(() -> userService.edit(query))
                .isInstanceOf(BizException.class);

        verify(userMapper, never()).updateById(any());
    }
}
