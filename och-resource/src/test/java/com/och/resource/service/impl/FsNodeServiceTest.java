package com.och.resource.service.impl;

import com.och.resource.domain.entity.FsNode;
import com.och.resource.mapper.FsNodeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * FsNodeService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class FsNodeServiceTest {

    @Mock
    private FsNodeMapper fsNodeMapper;

    @InjectMocks
    private FsNodeServiceImpl fsNodeService;

    // ==================== getById ====================

    @Test
    @DisplayName("根据 ID 查询节点")
    void testGetNodeById() {
        FsNode node = FsNode.builder()
                .id(1L)
                .nodeId("node-001")
                .nodeName("fs-01")
                .host("192.168.1.100")
                .port(8021)
                .password("ClueCon")
                .enabled(1)
                .build();

        when(fsNodeMapper.selectById(1L)).thenReturn(node);

        FsNode result = fsNodeService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("node-001", result.getNodeId());
        assertEquals("fs-01", result.getNodeName());
        assertEquals("192.168.1.100", result.getHost());
        assertEquals(8021, result.getPort());
        assertEquals("ClueCon", result.getPassword());
        assertEquals(1, result.getEnabled());
        verify(fsNodeMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据 ID 查询节点 - 记录不存在时返回 null")
    void testGetNodeByIdNotFound() {
        when(fsNodeMapper.selectById(999L)).thenReturn(null);

        FsNode result = fsNodeService.getById(999L);

        assertNull(result);
    }

    // ==================== add ====================

    @Test
    @DisplayName("注册新节点 - 正常保存并校验字段")
    void testRegisterNode() {
        FsNode node = FsNode.builder()
                .nodeId("node-002")
                .nodeName("fs-02")
                .host("10.0.0.50")
                .port(8021)
                .password("secret")
                .enabled(1)
                .remark("test node")
                .build();

        when(fsNodeMapper.insert(any(FsNode.class))).thenReturn(1);

        fsNodeService.add(node);

        ArgumentCaptor<FsNode> captor = ArgumentCaptor.forClass(FsNode.class);
        verify(fsNodeMapper).insert(captor.capture());
        FsNode saved = captor.getValue();
        assertEquals("node-002", saved.getNodeId());
        assertEquals("fs-02", saved.getNodeName());
        assertEquals("10.0.0.50", saved.getHost());
        assertEquals(8021, saved.getPort());
        assertEquals("secret", saved.getPassword());
        assertEquals(1, saved.getEnabled());
        assertEquals("test node", saved.getRemark());
    }

    // ==================== getEnabledNodes ====================

    @Test
    @DisplayName("查询所有已启用的节点列表")
    void testListEnabledNodes() {
        FsNode node1 = FsNode.builder().id(1L).nodeId("n1").nodeName("fs-01").host("10.0.0.1").enabled(1).build();
        FsNode node2 = FsNode.builder().id(2L).nodeId("n2").nodeName("fs-02").host("10.0.0.2").enabled(1).build();
        List<FsNode> expected = Arrays.asList(node1, node2);

        when(fsNodeMapper.findEnabledNodes()).thenReturn(expected);

        List<FsNode> result = fsNodeService.getEnabledNodes();

        assertEquals(2, result.size());
        assertEquals("n1", result.get(0).getNodeId());
        assertEquals("n2", result.get(1).getNodeId());
        verify(fsNodeMapper).findEnabledNodes();
    }

    @Test
    @DisplayName("查询所有已启用的节点 - 无启用节点时返回空列表")
    void testListEnabledNodesEmpty() {
        when(fsNodeMapper.findEnabledNodes()).thenReturn(Collections.emptyList());

        List<FsNode> result = fsNodeService.getEnabledNodes();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getAllNodes ====================

    @Test
    @DisplayName("查询全部节点列表（含禁用）")
    void testListAllNodes() {
        FsNode enabled = FsNode.builder().id(1L).nodeId("n1").nodeName("fs-01").enabled(1).build();
        FsNode disabled = FsNode.builder().id(2L).nodeId("n2").nodeName("fs-02").enabled(0).build();
        List<FsNode> expected = Arrays.asList(enabled, disabled);

        when(fsNodeMapper.selectList(any())).thenReturn(expected);

        List<FsNode> result = fsNodeService.getAllNodes();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getEnabled());
        assertEquals(0, result.get(1).getEnabled());
        verify(fsNodeMapper).selectList(any());
    }

    // ==================== update ====================

    @Test
    @DisplayName("更新节点信息")
    void testUpdateNode() {
        FsNode node = FsNode.builder()
                .id(1L)
                .nodeId("node-001")
                .nodeName("fs-01-updated")
                .host("192.168.2.100")
                .port(8022)
                .password("newpass")
                .enabled(0)
                .remark("updated remark")
                .build();

        when(fsNodeMapper.updateById(any(FsNode.class))).thenReturn(1);

        fsNodeService.update(node);

        ArgumentCaptor<FsNode> captor = ArgumentCaptor.forClass(FsNode.class);
        verify(fsNodeMapper).updateById(captor.capture());
        FsNode updated = captor.getValue();
        assertEquals(1L, updated.getId());
        assertEquals("node-001", updated.getNodeId());
        assertEquals("fs-01-updated", updated.getNodeName());
        assertEquals("192.168.2.100", updated.getHost());
        assertEquals(8022, updated.getPort());
        assertEquals("newpass", updated.getPassword());
        assertEquals(0, updated.getEnabled());
        assertEquals("updated remark", updated.getRemark());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除节点")
    void testDeleteNode() {
        when(fsNodeMapper.deleteById(1L)).thenReturn(1);

        fsNodeService.delete(1L);

        verify(fsNodeMapper).deleteById(1L);
    }
}
