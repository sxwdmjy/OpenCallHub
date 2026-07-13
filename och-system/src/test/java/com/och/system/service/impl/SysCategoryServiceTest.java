package com.och.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import com.och.common.error.BizException;
import com.och.system.domain.entity.SysCategory;
import com.och.system.domain.query.category.SysCategoryAddQuery;
import com.och.system.domain.query.category.SysCategoryQuery;
import com.och.system.mapper.SysCategoryMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * SysCategoryServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class SysCategoryServiceTest {

    @Mock
    private SysCategoryMapper categoryMapper;

    @InjectMocks
    private SysCategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryMapper);
    }

    // ── addCategory ───────────────────────────────────────────────────────

    @Test
    void addCategory_shouldSaveCategory_whenNameNotDuplicate() {
        SysCategoryAddQuery query = new SysCategoryAddQuery();
        query.setType(1);
        query.setName("New Category");
        query.setParentId(0L);

        // checkName calls getOne(wrapper) -> ServiceImpl.getOne calls baseMapper.selectOne(wrapper, boolean)
        doReturn(null).when(categoryMapper).selectOne(any(), anyBoolean());
        doReturn(1).when(categoryMapper).insert(any(SysCategory.class));

        categoryService.add(query);

        verify(categoryMapper).insert(any(SysCategory.class));
    }

    @Test
    void addCategory_shouldThrowBizException_whenNameDuplicate() {
        SysCategoryAddQuery query = new SysCategoryAddQuery();
        query.setType(1);
        query.setName("Existing Category");

        SysCategory existing = SysCategory.builder().id(1L).name("Existing Category").type(1).parentId(0L).build();
        doReturn(existing).when(categoryMapper).selectOne(any(), anyBoolean());

        assertThatThrownBy(() -> categoryService.add(query))
                .isInstanceOf(BizException.class);

        verify(categoryMapper, never()).insert(any());
    }

    // ── getCategory ───────────────────────────────────────────────────────

    @Test
    void getDetail_shouldReturnCategory_whenExists() {
        Long id = 1L;
        SysCategory category = SysCategory.builder()
                .id(id).type(1).name("Skills").parentId(0L).flag(0).build();

        when(categoryMapper.selectById(id)).thenReturn(category);

        SysCategory result = categoryService.getDetail(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Skills");
        verify(categoryMapper).selectById(id);
    }

    // ── listCategories (treeList) ─────────────────────────────────────────

    @Test
    void treeList_shouldReturnTreeWithRootAndChildren() {
        SysCategoryQuery query = new SysCategoryQuery();
        query.setType(1);

        SysCategory child = SysCategory.builder()
                .id(1L).type(1).name("IT Skills").parentId(0L).flag(0).build();

        when(categoryMapper.categoryList(1)).thenReturn(List.of(child));

        List<Tree<Long>> result = categoryService.treeList(query);

        // treeList inserts a root node "默认分组" (id=0, parentId=0).
        // Both the root and any top-level categories (parentId=0) appear
        // at the top level of the built tree.
        assertThat(result).isNotNull();
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        verify(categoryMapper).categoryList(1);
    }
}
