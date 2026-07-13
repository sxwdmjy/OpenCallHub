package com.och.system.service.impl;

import com.och.system.domain.entity.FsDialplan;
import com.och.system.domain.query.dialplan.FsDialplanAddQuery;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.vo.dialplan.FsDialplanVo;
import com.och.system.mapper.FsDialplanMapper;
import com.och.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * FsDialplanService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class FsDialplanServiceTest {

    @Mock
    private FsDialplanMapper fsDialplanMapper;

    @Mock
    private ISysUserService sysUserService;

    private FsDialplanServiceImpl fsDialplanService;

    @BeforeEach
    void setUp() {
        fsDialplanService = spy(new FsDialplanServiceImpl(sysUserService));
        ReflectionTestUtils.setField(fsDialplanService, "baseMapper", fsDialplanMapper);
        lenient().doReturn(true).when(fsDialplanService).updateBatchById(anyList());
    }

    // ==================== add ====================

    @Test
    @DisplayName("新增拨号计划 - 正常保存并校验字段映射")
    void testAddDialplan() {
        FsDialplanAddQuery query = new FsDialplanAddQuery();
        query.setGroupId(100L);
        query.setName("test-dialplan");
        query.setContextName("public");
        query.setType("xml");
        query.setContent("<extension name=\"test\">...</extension>");
        query.setDescribe("test description");

        when(fsDialplanMapper.insert(any(FsDialplan.class))).thenReturn(1);

        fsDialplanService.add(query);

        ArgumentCaptor<FsDialplan> captor = ArgumentCaptor.forClass(FsDialplan.class);
        verify(fsDialplanMapper).insert(captor.capture());
        FsDialplan saved = captor.getValue();
        assertEquals(100L, saved.getGroupId());
        assertEquals("test-dialplan", saved.getName());
        assertEquals("public", saved.getContextName());
        assertEquals("xml", saved.getType());
        assertEquals("<extension name=\"test\">...</extension>", saved.getContent());
        assertEquals("test description", saved.getDescribe());
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("根据 ID 查询拨号计划详情")
    void testGetDetail() {
        FsDialplan dialplan = FsDialplan.builder()
                .id(1L)
                .groupId(100L)
                .name("dialplan-1")
                .contextName("public")
                .type("xml")
                .content("<extension/>")
                .build();

        when(fsDialplanMapper.selectById(1L)).thenReturn(dialplan);

        FsDialplan result = fsDialplanService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("dialplan-1", result.getName());
        assertEquals("public", result.getContextName());
        verify(fsDialplanMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据 ID 查询拨号计划 - 记录不存在时返回 null")
    void testGetDetailNotFound() {
        when(fsDialplanMapper.selectById(999L)).thenReturn(null);

        FsDialplan result = fsDialplanService.getDetail(999L);

        assertNull(result);
        verify(fsDialplanMapper).selectById(999L);
    }

    // ==================== getList ====================

    @Test
    @DisplayName("查询拨号计划列表")
    void testGetList() {
        FsDialplanQuery query = new FsDialplanQuery();
        query.setName("dialplan");

        FsDialplanVo vo1 = new FsDialplanVo();
        vo1.setId(1L);
        vo1.setGroupId(100L);
        vo1.setName("dialplan-1");
        vo1.setContextName("public");

        FsDialplanVo vo2 = new FsDialplanVo();
        vo2.setId(2L);
        vo2.setGroupId(200L);
        vo2.setName("dialplan-2");
        vo2.setContextName("default");

        List<FsDialplanVo> expected = Arrays.asList(vo1, vo2);
        when(fsDialplanMapper.getList(query)).thenReturn(expected);

        List<FsDialplanVo> result = fsDialplanService.getList(query);

        assertEquals(2, result.size());
        assertEquals("dialplan-1", result.get(0).getName());
        assertEquals("dialplan-2", result.get(1).getName());
        verify(fsDialplanMapper).getList(query);
    }

    @Test
    @DisplayName("查询拨号计划列表 - 空结果")
    void testGetListEmpty() {
        FsDialplanQuery query = new FsDialplanQuery();
        query.setName("nonexistent");

        when(fsDialplanMapper.getList(query)).thenReturn(Collections.emptyList());

        List<FsDialplanVo> result = fsDialplanService.getList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除拨号计划 - 按单个 ID 逻辑删除")
    void testDeleteById() {
        FsDialplanQuery query = new FsDialplanQuery();
        query.setId(1L);

        fsDialplanService.delete(query);

        verify(fsDialplanService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除拨号计划 - 按 ID 列表批量逻辑删除")
    void testDeleteByIds() {
        FsDialplanQuery query = new FsDialplanQuery();
        query.setIds(Arrays.asList(1L, 2L, 3L));

        fsDialplanService.delete(query);

        verify(fsDialplanService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除拨号计划 - 空的 query（无 id 无 ids）不执行删除")
    void testDeleteEmpty() {
        FsDialplanQuery query = new FsDialplanQuery();

        fsDialplanService.delete(query);

        verify(fsDialplanService, never()).updateBatchById(anyList());
    }
}
