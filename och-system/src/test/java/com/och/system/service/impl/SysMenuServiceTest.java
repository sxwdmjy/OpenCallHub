package com.och.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import com.och.system.domain.entity.SysMenu;
import com.och.system.domain.query.menu.SysMenuAddQuery;
import com.och.system.domain.query.menu.SysMenuQuery;
import com.och.system.mapper.SysMenuMapper;
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
import static org.mockito.Mockito.*;

/**
 * SysMenuServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class SysMenuServiceTest {

    @Mock
    private SysMenuMapper menuMapper;

    @InjectMocks
    private SysMenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(menuService, "baseMapper", menuMapper);
    }

    // ── getMenuTree ───────────────────────────────────────────────────────

    @Test
    void buildMenuTree_shouldBuildHierarchicalTree_whenFlatListProvided() {
        // root menu
        SysMenu root = SysMenu.builder()
                .menuId(1L).parentId(0L).menuName("System Mgmt")
                .orderNum(1).menuType("M").path("/system").component("").build();
        // child menu
        SysMenu child = SysMenu.builder()
                .menuId(2L).parentId(1L).menuName("User Mgmt")
                .orderNum(1).menuType("C").path("user").component("system/user/index").build();
        // button under child
        SysMenu button = SysMenu.builder()
                .menuId(3L).parentId(2L).menuName("Add User")
                .orderNum(1).menuType("F").path("").component("").perms("system:user:add").build();

        List<SysMenu> flatList = List.of(root, child, button);

        List<Tree<Long>> tree = menuService.buildMenuTree(flatList);

        assertThat(tree).hasSize(1);
        Tree<Long> rootNode = tree.get(0);
        assertThat(rootNode.getId()).isEqualTo(1L);
        assertThat(rootNode.getChildren()).hasSize(1);

        Tree<Long> childNode = rootNode.getChildren().get(0);
        assertThat(childNode.getId()).isEqualTo(2L);
        assertThat(childNode.getChildren()).hasSize(1);
        assertThat(childNode.getChildren().get(0).getId()).isEqualTo(3L);
    }

    @Test
    void buildMenuTree_shouldReturnNull_whenInputEmpty() {
        List<Tree<Long>> tree = menuService.buildMenuTree(List.of());

        // Hutool TreeUtil.build returns null when input list is empty
        assertThat(tree).isNull();
    }

    // ── listMenusByRole ───────────────────────────────────────────────────

    @Test
    void getMenuListByRoleIds_shouldReturnMenusForGivenRoles() {
        List<Long> roleIds = List.of(1L, 2L);
        SysMenu menu1 = SysMenu.builder().menuId(1L).menuName("Dashboard").build();
        SysMenu menu2 = SysMenu.builder().menuId(2L).menuName("Settings").build();

        when(menuMapper.getMenuListByRoleIds(roleIds)).thenReturn(List.of(menu1, menu2));

        List<SysMenu> result = menuService.getMenuListByRoleIds(roleIds);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SysMenu::getMenuName).containsExactly("Dashboard", "Settings");
        verify(menuMapper).getMenuListByRoleIds(roleIds);
    }

    // ── listMenus ─────────────────────────────────────────────────────────

    @Test
    void getList_shouldReturnAllMenus() {
        SysMenuQuery query = new SysMenuQuery();
        SysMenu menu1 = SysMenu.builder().menuId(1L).menuName("Dashboard").build();
        SysMenu menu2 = SysMenu.builder().menuId(2L).menuName("Settings").build();

        when(menuMapper.getList(query)).thenReturn(List.of(menu1, menu2));

        List<SysMenu> result = menuService.getList(query);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMenuName()).isEqualTo("Dashboard");
        verify(menuMapper).getList(query);
    }

    // ── addMenu ───────────────────────────────────────────────────────────

    @Test
    void addMenu_shouldSaveMenu() {
        SysMenuAddQuery query = new SysMenuAddQuery();
        query.setMenuName("New Menu");
        query.setParentId(0L);
        query.setMenuType("M");
        query.setOrderNum(1);

        when(menuMapper.insert(any(SysMenu.class))).thenReturn(1);

        menuService.add(query);

        verify(menuMapper).insert(any(SysMenu.class));
    }
}
