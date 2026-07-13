package com.och.system.service.impl;

import com.och.system.domain.entity.FsModules;
import com.och.system.domain.query.modules.FsModulesAddQuery;
import com.och.system.domain.query.modules.FsModulesQuery;
import com.och.system.mapper.FsModulesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
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
 * FsModulesService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class FsModulesServiceTest {

    @Mock
    private FsModulesMapper fsModulesMapper;

    @Spy
    private FsModulesServiceImpl fsModulesService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fsModulesService, "baseMapper", fsModulesMapper);
        lenient().doReturn(true).when(fsModulesService).updateBatchById(anyList());
    }

    // ==================== add ====================

    @Test
    @DisplayName("新增模块 - 正常保存并校验字段映射")
    void testAddModule() {
        FsModulesAddQuery query = new FsModulesAddQuery();
        query.setFsName("fs-node-1");
        query.setName("mod_sofia");
        query.setType("xml");
        query.setContent("<configuration name=\"sofia.conf\">...</configuration>");
        query.setDescribe("Sofia SIP module configuration");

        when(fsModulesMapper.insert(any(FsModules.class))).thenReturn(1);

        fsModulesService.add(query);

        ArgumentCaptor<FsModules> captor = ArgumentCaptor.forClass(FsModules.class);
        verify(fsModulesMapper).insert(captor.capture());
        FsModules saved = captor.getValue();
        assertEquals("fs-node-1", saved.getFsName());
        assertEquals("mod_sofia", saved.getName());
        assertEquals("xml", saved.getType());
        assertEquals("<configuration name=\"sofia.conf\">...</configuration>", saved.getContent());
        assertEquals("Sofia SIP module configuration", saved.getDescribe());
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("根据 ID 查询模块详情")
    void testGetDetail() {
        FsModules module = FsModules.builder()
                .id(1L)
                .fsName("fs-node-1")
                .name("mod_sofia")
                .type("xml")
                .build();

        when(fsModulesMapper.selectById(1L)).thenReturn(module);

        FsModules result = fsModulesService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("fs-node-1", result.getFsName());
        assertEquals("mod_sofia", result.getName());
        verify(fsModulesMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据 ID 查询模块 - 记录不存在时返回 null")
    void testGetDetailNotFound() {
        when(fsModulesMapper.selectById(999L)).thenReturn(null);

        FsModules result = fsModulesService.getDetail(999L);

        assertNull(result);
        verify(fsModulesMapper).selectById(999L);
    }

    // ==================== getList ====================

    @Test
    @DisplayName("查询模块列表")
    void testGetList() {
        FsModulesQuery query = new FsModulesQuery();
        query.setFsName("fs-node");

        FsModules m1 = FsModules.builder().id(1L).fsName("fs-node-1").name("mod_sofia").type("xml").build();
        FsModules m2 = FsModules.builder().id(2L).fsName("fs-node-1").name("mod_event_socket").type("xml").build();
        List<FsModules> expected = Arrays.asList(m1, m2);

        when(fsModulesMapper.getList(query)).thenReturn(expected);

        List<FsModules> result = fsModulesService.getList(query);

        assertEquals(2, result.size());
        assertEquals("mod_sofia", result.get(0).getName());
        assertEquals("mod_event_socket", result.get(1).getName());
        verify(fsModulesMapper).getList(query);
    }

    @Test
    @DisplayName("查询模块列表 - 空结果")
    void testGetListEmpty() {
        FsModulesQuery query = new FsModulesQuery();
        query.setFsName("nonexistent");

        when(fsModulesMapper.getList(query)).thenReturn(Collections.emptyList());

        List<FsModules> result = fsModulesService.getList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getModuleByName ====================

    @Test
    @DisplayName("根据名称查询模块")
    void testGetModuleByName() {
        FsModules module = FsModules.builder()
                .id(1L)
                .fsName("fs-node-1")
                .name("mod_sofia")
                .type("xml")
                .build();

        when(fsModulesMapper.getList(any(FsModulesQuery.class))).thenReturn(Arrays.asList(module));

        FsModules result = fsModulesService.getModuleByName("mod_sofia");

        assertNotNull(result);
        assertEquals("mod_sofia", result.getName());
        verify(fsModulesMapper).getList(any(FsModulesQuery.class));
    }

    @Test
    @DisplayName("根据名称查询模块 - 不存在时返回 null")
    void testGetModuleByNameNotFound() {
        when(fsModulesMapper.getList(any(FsModulesQuery.class))).thenReturn(Collections.emptyList());

        FsModules result = fsModulesService.getModuleByName("nonexistent");

        assertNull(result);
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除模块 - 按单个 ID 逻辑删除")
    void testDeleteById() {
        FsModulesQuery query = new FsModulesQuery();
        query.setId(1L);

        fsModulesService.delete(query);

        verify(fsModulesService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除模块 - 按 ID 列表批量逻辑删除")
    void testDeleteByIds() {
        FsModulesQuery query = new FsModulesQuery();
        query.setIds(Arrays.asList(1L, 2L, 3L));

        fsModulesService.delete(query);

        verify(fsModulesService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除模块 - 空的 query（无 id 无 ids）不执行删除")
    void testDeleteEmpty() {
        FsModulesQuery query = new FsModulesQuery();

        fsModulesService.delete(query);

        verify(fsModulesService, never()).updateBatchById(anyList());
    }
}
