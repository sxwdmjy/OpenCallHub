package com.och.system.service.impl;

import com.och.common.enums.DeleteStatusEnum;
import com.och.common.error.BizException;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fssip.FsSipGatewayAddQuery;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import com.och.system.mapper.FsSipGatewayMapper;
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
 * FsSipGatewayService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class FsSipGatewayServiceTest {

    @Mock
    private FsSipGatewayMapper fsSipGatewayMapper;

    @Spy
    private FsSipGatewayServiceImpl fsSipGatewayService;

    @BeforeEach
    void setUp() {
        // Inject mock mapper into the parent ServiceImpl.baseMapper field
        ReflectionTestUtils.setField(fsSipGatewayService, "baseMapper", fsSipGatewayMapper);
        // Stub updateBatchById to avoid MyBatis-Plus TableInfoCache dependency in unit tests.
        // lenient() because not all tests call updateBatchById.
        lenient().doReturn(true).when(fsSipGatewayService).updateBatchById(anyList());
    }

    // ==================== add ====================

    @Test
    @DisplayName("新增 SIP 网关 - 正常保存并校验字段映射")
    void testAddGateway() {
        FsSipGatewayAddQuery query = new FsSipGatewayAddQuery();
        query.setName("gateway-1");
        query.setUserName("user1");
        query.setPassword("pass1");
        query.setRealm("192.168.1.1");
        query.setProxy("192.168.1.1:5060");
        query.setRegister(1);
        query.setTransport(1);
        query.setFromDomain("example.com");
        query.setRetryTime(30);
        query.setPingTime(60);
        query.setGatewayType(1);

        when(fsSipGatewayMapper.insert(any(FsSipGateway.class))).thenReturn(1);

        fsSipGatewayService.add(query);

        ArgumentCaptor<FsSipGateway> captor = ArgumentCaptor.forClass(FsSipGateway.class);
        verify(fsSipGatewayMapper).insert(captor.capture());
        FsSipGateway saved = captor.getValue();
        assertEquals("gateway-1", saved.getName());
        assertEquals("user1", saved.getUserName());
        assertEquals("pass1", saved.getPassword());
        assertEquals("192.168.1.1", saved.getRealm());
        assertEquals("192.168.1.1:5060", saved.getProxy());
        assertEquals(1, saved.getRegister());
        assertEquals(1, saved.getTransport());
        assertEquals("example.com", saved.getFromDomain());
        assertEquals(30, saved.getRetryTime());
        assertEquals(60, saved.getPingTime());
        assertEquals(1, saved.getGatewayType());
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("根据 ID 查询 SIP 网关详情")
    void testGetDetail() {
        FsSipGateway gateway = FsSipGateway.builder()
                .id(1L)
                .name("gw-1")
                .userName("user1")
                .realm("192.168.1.1")
                .proxy("192.168.1.1:5060")
                .register(1)
                .transport(1)
                .gatewayType(1)
                .build();

        when(fsSipGatewayMapper.selectById(1L)).thenReturn(gateway);

        FsSipGateway result = fsSipGatewayService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("gw-1", result.getName());
        assertEquals("192.168.1.1:5060", result.getProxy());
        verify(fsSipGatewayMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据 ID 查询 SIP 网关 - 记录不存在时返回 null")
    void testGetDetailNotFound() {
        when(fsSipGatewayMapper.selectById(999L)).thenReturn(null);

        FsSipGateway result = fsSipGatewayService.getDetail(999L);

        assertNull(result);
    }

    // ==================== getList ====================

    @Test
    @DisplayName("查询 SIP 网关列表")
    void testGetList() {
        FsSipGatewayQuery query = new FsSipGatewayQuery();
        query.setName("gw");
        query.setType(1);

        FsSipGateway gw1 = FsSipGateway.builder().id(1L).name("gw-1").type(1).build();
        FsSipGateway gw2 = FsSipGateway.builder().id(2L).name("gw-2").type(1).build();
        List<FsSipGateway> expected = Arrays.asList(gw1, gw2);

        when(fsSipGatewayMapper.getList(query)).thenReturn(expected);

        List<FsSipGateway> result = fsSipGatewayService.getList(query);

        assertEquals(2, result.size());
        assertEquals("gw-1", result.get(0).getName());
        assertEquals("gw-2", result.get(1).getName());
        verify(fsSipGatewayMapper).getList(query);
    }

    @Test
    @DisplayName("查询 SIP 网关列表 - 空结果")
    void testGetListEmpty() {
        FsSipGatewayQuery query = new FsSipGatewayQuery();
        query.setName("nonexistent");

        when(fsSipGatewayMapper.getList(query)).thenReturn(Collections.emptyList());

        List<FsSipGateway> result = fsSipGatewayService.getList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== edit ====================

    @Test
    @DisplayName("编辑 SIP 网关 - 正常更新")
    void testEditGateway() {
        FsSipGateway existing = FsSipGateway.builder()
                .id(1L)
                .name("old-gw")
                .realm("10.0.0.1")
                .proxy("10.0.0.1:5060")
                .build();

        FsSipGatewayAddQuery query = new FsSipGatewayAddQuery();
        query.setId(1L);
        query.setName("updated-gw");
        query.setUserName("newuser");
        query.setPassword("newpass");
        query.setRealm("192.168.2.1");
        query.setProxy("192.168.2.1:5060");
        query.setRegister(1);
        query.setTransport(2);
        query.setFromDomain("newexample.com");
        query.setRetryTime(45);
        query.setPingTime(120);
        query.setGatewayType(0);

        when(fsSipGatewayMapper.selectById(1L)).thenReturn(existing);
        when(fsSipGatewayMapper.updateById(any(FsSipGateway.class))).thenReturn(1);

        fsSipGatewayService.edit(query);

        ArgumentCaptor<FsSipGateway> captor = ArgumentCaptor.forClass(FsSipGateway.class);
        verify(fsSipGatewayMapper).updateById(captor.capture());
        FsSipGateway updated = captor.getValue();
        assertEquals(1L, updated.getId());
        assertEquals("updated-gw", updated.getName());
        assertEquals("newuser", updated.getUserName());
        assertEquals("192.168.2.1", updated.getRealm());
        assertEquals("192.168.2.1:5060", updated.getProxy());
        assertEquals(2, updated.getTransport());
        assertEquals("newexample.com", updated.getFromDomain());
        assertEquals(45, updated.getRetryTime());
        assertEquals(120, updated.getPingTime());
        assertEquals(0, updated.getGatewayType());
    }

    @Test
    @DisplayName("编辑 SIP 网关 - 记录不存在时抛出 BizException")
    void testEditGatewayNotFound() {
        FsSipGatewayAddQuery query = new FsSipGatewayAddQuery();
        query.setId(999L);
        query.setName("ghost-gw");

        when(fsSipGatewayMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> fsSipGatewayService.edit(query));
        verify(fsSipGatewayMapper, never()).updateById(any());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除 SIP 网关 - 按单个 ID 逻辑删除")
    void testDeleteById() {
        FsSipGatewayQuery query = new FsSipGatewayQuery();
        query.setId(1L);

        fsSipGatewayService.delete(query);

        verify(fsSipGatewayService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除 SIP 网关 - 按 ID 列表批量逻辑删除")
    void testDeleteByIds() {
        FsSipGatewayQuery query = new FsSipGatewayQuery();
        query.setIds(Arrays.asList(1L, 2L, 3L));

        fsSipGatewayService.delete(query);

        verify(fsSipGatewayService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除 SIP 网关 - 空的 query 不执行删除")
    void testDeleteEmpty() {
        FsSipGatewayQuery query = new FsSipGatewayQuery();

        fsSipGatewayService.delete(query);

        verify(fsSipGatewayService, never()).updateBatchById(anyList());
    }
}
