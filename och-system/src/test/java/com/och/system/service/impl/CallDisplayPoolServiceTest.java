package com.och.system.service.impl;

import com.och.system.domain.entity.CallDisplayPool;
import com.och.system.domain.query.display.CallDisplayPoolAddQuery;
import com.och.system.domain.query.display.CallDisplayPoolQuery;
import com.och.system.domain.vo.display.CallDisplayPoolVo;
import com.och.system.mapper.CallDisplayPoolMapper;
import com.och.system.service.ICallDisplayPoolRelService;
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
 * CallDisplayPoolService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class CallDisplayPoolServiceTest {

    @Mock
    private CallDisplayPoolMapper callDisplayPoolMapper;

    @Mock
    private ICallDisplayPoolRelService callDisplayPoolRelService;

    @Mock
    private ISysUserService sysUserService;

    private CallDisplayPoolServiceImpl callDisplayPoolService;

    @BeforeEach
    void setUp() {
        callDisplayPoolService = spy(new CallDisplayPoolServiceImpl(callDisplayPoolRelService, sysUserService));
        ReflectionTestUtils.setField(callDisplayPoolService, "baseMapper", callDisplayPoolMapper);
        lenient().doReturn(true).when(callDisplayPoolService).updateBatchById(anyList());
    }

    // ==================== addPool ====================

    @Test
    @DisplayName("新增号码池 - 正常保存并建立关联")
    void testAddPool() {
        CallDisplayPoolAddQuery query = new CallDisplayPoolAddQuery();
        query.setName("pool-1");
        query.setType(1);
        query.setPhoneList(Arrays.asList(10L, 20L));

        when(callDisplayPoolMapper.insert(any(CallDisplayPool.class))).thenReturn(1);

        callDisplayPoolService.addPool(query);

        ArgumentCaptor<CallDisplayPool> captor = ArgumentCaptor.forClass(CallDisplayPool.class);
        verify(callDisplayPoolMapper).insert(captor.capture());
        CallDisplayPool saved = captor.getValue();
        assertEquals("pool-1", saved.getName());
        assertEquals(1, saved.getType());

        // verify association was called
        verify(callDisplayPoolRelService).addByPoolId(any(), eq(Arrays.asList(10L, 20L)));
    }

    // ==================== getPoolDetail ====================

    @Test
    @DisplayName("根据 ID 查询号码池详情")
    void testGetPoolDetail() {
        CallDisplayPoolVo vo = new CallDisplayPoolVo();
        vo.setId(1L);
        vo.setName("pool-1");
        vo.setType(1);

        when(callDisplayPoolMapper.getPoolDetail(1L)).thenReturn(vo);

        CallDisplayPoolVo result = callDisplayPoolService.getPoolDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("pool-1", result.getName());
        assertEquals(1, result.getType());
        verify(callDisplayPoolMapper).getPoolDetail(1L);
    }

    @Test
    @DisplayName("根据 ID 查询号码池详情 - 记录不存在时返回 null")
    void testGetPoolDetailNotFound() {
        when(callDisplayPoolMapper.getPoolDetail(999L)).thenReturn(null);

        CallDisplayPoolVo result = callDisplayPoolService.getPoolDetail(999L);

        assertNull(result);
        verify(callDisplayPoolMapper).getPoolDetail(999L);
    }

    // ==================== getPoolList ====================

    @Test
    @DisplayName("查询号码池列表")
    void testGetPoolList() {
        CallDisplayPoolQuery query = new CallDisplayPoolQuery();
        query.setName("pool");

        CallDisplayPoolVo vo1 = new CallDisplayPoolVo();
        vo1.setId(1L);
        vo1.setName("pool-1");
        vo1.setType(1);

        CallDisplayPoolVo vo2 = new CallDisplayPoolVo();
        vo2.setId(2L);
        vo2.setName("pool-2");
        vo2.setType(2);

        List<CallDisplayPoolVo> expected = Arrays.asList(vo1, vo2);
        when(callDisplayPoolMapper.getList(query)).thenReturn(expected);

        List<CallDisplayPoolVo> result = callDisplayPoolService.getPoolList(query);

        assertEquals(2, result.size());
        assertEquals("pool-1", result.get(0).getName());
        assertEquals("pool-2", result.get(1).getName());
        verify(callDisplayPoolMapper).getList(query);
    }

    @Test
    @DisplayName("查询号码池列表 - 空结果")
    void testGetPoolListEmpty() {
        CallDisplayPoolQuery query = new CallDisplayPoolQuery();
        query.setName("nonexistent");

        when(callDisplayPoolMapper.getList(query)).thenReturn(Collections.emptyList());

        List<CallDisplayPoolVo> result = callDisplayPoolService.getPoolList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== deletePool ====================

    @Test
    @DisplayName("删除号码池 - 按单个 ID 逻辑删除并清除关联")
    void testDeletePoolById() {
        CallDisplayPoolQuery query = new CallDisplayPoolQuery();
        query.setId(1L);

        callDisplayPoolService.deletePool(query);

        verify(callDisplayPoolService).updateBatchById(anyList());
        verify(callDisplayPoolRelService).delByPoolId(eq(1L));
    }

    @Test
    @DisplayName("删除号码池 - 按 ID 列表批量逻辑删除")
    void testDeletePoolByIds() {
        CallDisplayPoolQuery query = new CallDisplayPoolQuery();
        query.setIds(Arrays.asList(1L, 2L, 3L));

        callDisplayPoolService.deletePool(query);

        verify(callDisplayPoolService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除号码池 - 空的 query（无 id 无 ids）不执行删除")
    void testDeletePoolEmpty() {
        CallDisplayPoolQuery query = new CallDisplayPoolQuery();

        callDisplayPoolService.deletePool(query);

        verify(callDisplayPoolService, never()).updateBatchById(anyList());
        verify(callDisplayPoolRelService, never()).delByPoolId(anyLong());
    }
}
