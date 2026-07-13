package com.och.system.service.impl;

import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.FsConfig;
import com.och.system.domain.query.fsconfig.FsConfigAddQuery;
import com.och.system.domain.query.fsconfig.FsConfigQuery;
import com.och.system.mapper.FsConfigMapper;
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
 * FsConfigService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class FsConfigServiceTest {

    @Mock
    private FsConfigMapper fsConfigMapper;

    @Spy
    private FsConfigServiceImpl fsConfigService;

    @BeforeEach
    void setUp() {
        // Inject mock mapper into the parent ServiceImpl.baseMapper field
        ReflectionTestUtils.setField(fsConfigService, "baseMapper", fsConfigMapper);
        // Stub updateBatchById to avoid MyBatis-Plus TableInfoCache dependency in unit tests.
        // lenient() because not all tests call updateBatchById.
        lenient().doReturn(true).when(fsConfigService).updateBatchById(anyList());
    }

    // ==================== add ====================

    @Test
    @DisplayName("新增 FS 配置 - 正常保存并校验字段映射")
    void testAddConfig() {
        FsConfigAddQuery query = new FsConfigAddQuery();
        query.setName("test-node");
        query.setGroup("default");
        query.setIp("192.168.1.100");
        query.setPort(8021);
        query.setUserName("admin");
        query.setPassword("secret");
        query.setStatus(0);
        query.setOutTime(30);

        when(fsConfigMapper.insert(any(FsConfig.class))).thenReturn(1);

        fsConfigService.add(query);

        ArgumentCaptor<FsConfig> captor = ArgumentCaptor.forClass(FsConfig.class);
        verify(fsConfigMapper).insert(captor.capture());
        FsConfig saved = captor.getValue();
        assertEquals("test-node", saved.getName());
        assertEquals("default", saved.getGroup());
        assertEquals("192.168.1.100", saved.getIp());
        assertEquals(8021, saved.getPort());
        assertEquals("admin", saved.getUserName());
        assertEquals("secret", saved.getPassword());
        assertEquals(0, saved.getStatus());
        assertEquals(30, saved.getOutTime());
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("根据 ID 查询 FS 配置详情")
    void testGetDetail() {
        FsConfig config = FsConfig.builder()
                .id(1)
                .name("node-1")
                .group("default")
                .ip("10.0.0.1")
                .port(8021)
                .userName("admin")
                .password("pwd")
                .status(0)
                .build();

        when(fsConfigMapper.selectById(1)).thenReturn(config);

        FsConfig result = fsConfigService.getDetail(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("node-1", result.getName());
        assertEquals("10.0.0.1", result.getIp());
        verify(fsConfigMapper).selectById(1);
    }

    @Test
    @DisplayName("根据 ID 查询 FS 配置 - 记录不存在时返回 null")
    void testGetDetailNotFound() {
        when(fsConfigMapper.selectById(999)).thenReturn(null);

        FsConfig result = fsConfigService.getDetail(999);

        assertNull(result);
        verify(fsConfigMapper).selectById(999);
    }

    // ==================== getList ====================

    @Test
    @DisplayName("查询 FS 配置列表")
    void testGetList() {
        FsConfigQuery query = new FsConfigQuery();
        query.setName("node");
        query.setGroup("default");

        FsConfig config1 = FsConfig.builder().id(1).name("node-1").group("default").build();
        FsConfig config2 = FsConfig.builder().id(2).name("node-2").group("default").build();
        List<FsConfig> expected = Arrays.asList(config1, config2);

        when(fsConfigMapper.getList(query)).thenReturn(expected);

        List<FsConfig> result = fsConfigService.getList(query);

        assertEquals(2, result.size());
        assertEquals("node-1", result.get(0).getName());
        assertEquals("node-2", result.get(1).getName());
        verify(fsConfigMapper).getList(query);
    }

    @Test
    @DisplayName("查询 FS 配置列表 - 空结果")
    void testGetListEmpty() {
        FsConfigQuery query = new FsConfigQuery();
        query.setName("nonexistent");

        when(fsConfigMapper.getList(query)).thenReturn(Collections.emptyList());

        List<FsConfig> result = fsConfigService.getList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== edit ====================

    @Test
    @DisplayName("编辑 FS 配置 - 正常更新")
    void testEditConfig() {
        FsConfigAddQuery query = new FsConfigAddQuery();
        query.setId(1);
        query.setName("updated-node");
        query.setGroup("production");
        query.setIp("10.0.0.99");
        query.setPort(8022);
        query.setUserName("superadmin");
        query.setPassword("newsecret");
        query.setStatus(0);
        query.setOutTime(60);

        when(fsConfigMapper.updateById(any(FsConfig.class))).thenReturn(1);

        fsConfigService.edit(query);

        ArgumentCaptor<FsConfig> captor = ArgumentCaptor.forClass(FsConfig.class);
        verify(fsConfigMapper).updateById(captor.capture());
        FsConfig updated = captor.getValue();
        assertEquals("updated-node", updated.getName());
        assertEquals("production", updated.getGroup());
        assertEquals("10.0.0.99", updated.getIp());
        assertEquals(8022, updated.getPort());
        assertEquals("superadmin", updated.getUserName());
        assertEquals("newsecret", updated.getPassword());
        assertEquals(60, updated.getOutTime());
    }

    // ==================== updateStatusById ====================

    @Test
    @DisplayName("更新 FS 配置状态")
    void testUpdateStatusById() {
        when(fsConfigMapper.updateById(any(FsConfig.class))).thenReturn(1);

        fsConfigService.updateStatusById(1, 1);

        ArgumentCaptor<FsConfig> captor = ArgumentCaptor.forClass(FsConfig.class);
        verify(fsConfigMapper).updateById(captor.capture());
        FsConfig updated = captor.getValue();
        assertEquals(1, updated.getId());
        assertEquals(1, updated.getStatus());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除 FS 配置 - 按单个 ID 逻辑删除")
    void testDeleteById() {
        FsConfigQuery query = new FsConfigQuery();
        query.setId(1);

        fsConfigService.delete(query);

        // Verify updateBatchById was called on the service (stubbed in setUp)
        verify(fsConfigService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除 FS 配置 - 按 ID 列表批量逻辑删除")
    void testDeleteByIds() {
        FsConfigQuery query = new FsConfigQuery();
        query.setIds(Arrays.asList(1, 2, 3));

        fsConfigService.delete(query);

        verify(fsConfigService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除 FS 配置 - 空的 query（无 id 无 ids）不执行删除")
    void testDeleteEmpty() {
        FsConfigQuery query = new FsConfigQuery();

        fsConfigService.delete(query);

        verify(fsConfigService, never()).updateBatchById(anyList());
    }
}
