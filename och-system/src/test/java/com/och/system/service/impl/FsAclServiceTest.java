package com.och.system.service.impl;

import com.och.common.enums.DeleteStatusEnum;
import com.och.common.error.BizException;
import com.och.system.domain.entity.FsAcl;
import com.och.system.domain.query.acl.FsAclAddQuery;
import com.och.system.domain.query.acl.FsAclNodeAddQuery;
import com.och.system.domain.query.acl.FsAclQuery;
import com.och.system.domain.vo.acl.FsAclVo;
import com.och.system.mapper.FsAclMapper;
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
 * FsAclService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class FsAclServiceTest {

    @Mock
    private FsAclMapper fsAclMapper;

    @Mock
    private ISysUserService sysUserService;

    private FsAclServiceImpl fsAclService;

    @BeforeEach
    void setUp() {
        fsAclService = spy(new FsAclServiceImpl(sysUserService));
        ReflectionTestUtils.setField(fsAclService, "baseMapper", fsAclMapper);
        // Stub updateBatchById to avoid MyBatis-Plus TableInfoCache dependency in unit tests
        lenient().doReturn(true).when(fsAclService).updateBatchById(anyList());
    }

    // ==================== addList ====================

    @Test
    @DisplayName("新增 ACL 列表 - 正常保存并校验字段映射")
    void testAddList() {
        FsAclAddQuery query = new FsAclAddQuery();
        query.setName("test-acl");
        query.setDefaultType("allow");

        when(fsAclMapper.insert(any(FsAcl.class))).thenReturn(1);

        fsAclService.addList(query);

        ArgumentCaptor<FsAcl> captor = ArgumentCaptor.forClass(FsAcl.class);
        verify(fsAclMapper).insert(captor.capture());
        FsAcl saved = captor.getValue();
        assertEquals("test-acl", saved.getName());
        assertEquals("allow", saved.getDefaultType());
        assertNull(saved.getListId());
        assertNull(saved.getCidr());
    }

    // ==================== addNode ====================

    @Test
    @DisplayName("新增 ACL 节点 - 正常保存并校验字段映射")
    void testAddNode() {
        FsAclNodeAddQuery query = new FsAclNodeAddQuery();
        query.setListId(1L);
        query.setName("node-1");
        query.setDefaultType("allow");
        query.setNodeType("allow");
        query.setCidr("192.168.1.0/24");
        query.setDomain("example.com");

        when(fsAclMapper.insert(any(FsAcl.class))).thenReturn(1);

        fsAclService.addNode(query);

        ArgumentCaptor<FsAcl> captor = ArgumentCaptor.forClass(FsAcl.class);
        verify(fsAclMapper).insert(captor.capture());
        FsAcl saved = captor.getValue();
        assertEquals(1L, saved.getListId());
        assertEquals("node-1", saved.getName());
        assertEquals("allow", saved.getDefaultType());
        assertEquals("allow", saved.getNodeType());
        assertEquals("192.168.1.0/24", saved.getCidr());
        assertEquals("example.com", saved.getDomain());
    }

    @Test
    @DisplayName("新增 ACL 节点 - listId 为空时抛 BizException")
    void testAddNodeWithoutListId() {
        FsAclNodeAddQuery query = new FsAclNodeAddQuery();
        query.setName("node-1");
        // listId not set

        assertThrows(BizException.class, () -> fsAclService.addNode(query));
        verify(fsAclMapper, never()).insert(any());
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("根据 ID 查询 ACL 详情")
    void testGetDetail() {
        FsAcl acl = FsAcl.builder()
                .id(1L)
                .name("my-acl")
                .defaultType("deny")
                .build();

        when(fsAclMapper.selectById(1L)).thenReturn(acl);

        FsAcl result = fsAclService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("my-acl", result.getName());
        assertEquals("deny", result.getDefaultType());
        verify(fsAclMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据 ID 查询 ACL 详情 - 记录不存在时返回 null")
    void testGetDetailNotFound() {
        when(fsAclMapper.selectById(999L)).thenReturn(null);

        FsAcl result = fsAclService.getDetail(999L);

        assertNull(result);
        verify(fsAclMapper).selectById(999L);
    }

    // ==================== getList ====================

    @Test
    @DisplayName("查询 ACL 列表")
    void testGetList() {
        FsAclQuery query = new FsAclQuery();
        query.setName("acl");

        FsAclVo vo1 = new FsAclVo();
        vo1.setId(1L);
        vo1.setName("acl-1");
        vo1.setDefaultType("allow");

        FsAclVo vo2 = new FsAclVo();
        vo2.setId(2L);
        vo2.setName("acl-2");
        vo2.setDefaultType("deny");

        List<FsAclVo> expected = Arrays.asList(vo1, vo2);
        when(fsAclMapper.getList(query)).thenReturn(expected);

        List<FsAclVo> result = fsAclService.getList(query);

        assertEquals(2, result.size());
        assertEquals("acl-1", result.get(0).getName());
        assertEquals("acl-2", result.get(1).getName());
        verify(fsAclMapper).getList(query);
    }

    @Test
    @DisplayName("查询 ACL 列表 - 空结果")
    void testGetListEmpty() {
        FsAclQuery query = new FsAclQuery();
        query.setName("nonexistent");

        when(fsAclMapper.getList(query)).thenReturn(Collections.emptyList());

        List<FsAclVo> result = fsAclService.getList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除 ACL - 按单个 ID 逻辑删除")
    void testDeleteById() {
        FsAclQuery query = new FsAclQuery();
        query.setId(1L);

        fsAclService.delete(query);

        verify(fsAclService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除 ACL - 按 ID 列表批量逻辑删除")
    void testDeleteByIds() {
        FsAclQuery query = new FsAclQuery();
        query.setIds(Arrays.asList(1L, 2L, 3L));

        fsAclService.delete(query);

        verify(fsAclService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除 ACL - 空的 query（无 id 无 ids）不执行删除")
    void testDeleteEmpty() {
        FsAclQuery query = new FsAclQuery();

        fsAclService.delete(query);

        verify(fsAclService, never()).updateBatchById(anyList());
    }
}
