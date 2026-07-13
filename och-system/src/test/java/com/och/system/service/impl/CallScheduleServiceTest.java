package com.och.system.service.impl;

import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.query.schedule.CallScheduleAddQuery;
import com.och.system.domain.query.schedule.CallScheduleQuery;
import com.och.system.mapper.CallScheduleMapper;
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
 * CallScheduleService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class CallScheduleServiceTest {

    @Mock
    private CallScheduleMapper callScheduleMapper;

    @Spy
    private CallScheduleServiceImpl callScheduleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(callScheduleService, "baseMapper", callScheduleMapper);
        lenient().doReturn(true).when(callScheduleService).updateBatchById(anyList());
    }

    // ==================== add ====================

    @Test
    @DisplayName("新增日程 - 正常保存并校验字段映射")
    void testAddSchedule() {
        CallScheduleAddQuery query = new CallScheduleAddQuery();
        query.setName("work-hours");
        query.setLevel(5);
        query.setType(0);
        query.setStartDay("2024-01-01");
        query.setEndDay("2024-12-31");
        query.setStartTime("09:00");
        query.setEndTime("18:00");
        query.setWorkCycle("1,2,3,4,5");

        when(callScheduleMapper.insert(any(CallSchedule.class))).thenReturn(1);

        callScheduleService.add(query);

        ArgumentCaptor<CallSchedule> captor = ArgumentCaptor.forClass(CallSchedule.class);
        verify(callScheduleMapper).insert(captor.capture());
        CallSchedule saved = captor.getValue();
        assertEquals("work-hours", saved.getName());
        assertEquals(5, saved.getLevel());
        assertEquals(0, saved.getType());
        assertEquals("2024-01-01", saved.getStartDay());
        assertEquals("2024-12-31", saved.getEndDay());
        assertEquals("09:00", saved.getStartTime());
        assertEquals("18:00", saved.getEndTime());
        assertEquals("1,2,3,4,5", saved.getWorkCycle());
    }

    // ==================== getDetail ====================

    @Test
    @DisplayName("根据 ID 查询日程详情")
    void testGetDetail() {
        CallSchedule schedule = CallSchedule.builder()
                .id(1L)
                .name("work-hours")
                .level(5)
                .type(0)
                .startDay("2024-01-01")
                .endDay("2024-12-31")
                .build();

        when(callScheduleMapper.selectById(1L)).thenReturn(schedule);

        CallSchedule result = callScheduleService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("work-hours", result.getName());
        assertEquals(5, result.getLevel());
        verify(callScheduleMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据 ID 查询日程 - 记录不存在时返回 null")
    void testGetDetailNotFound() {
        when(callScheduleMapper.selectById(999L)).thenReturn(null);

        CallSchedule result = callScheduleService.getDetail(999L);

        assertNull(result);
        verify(callScheduleMapper).selectById(999L);
    }

    // ==================== getList ====================

    @Test
    @DisplayName("查询日程列表")
    void testGetList() {
        CallScheduleQuery query = new CallScheduleQuery();
        query.setName("work");

        CallSchedule s1 = CallSchedule.builder().id(1L).name("work-hours").level(5).type(0).build();
        CallSchedule s2 = CallSchedule.builder().id(2L).name("work-weekend").level(3).type(0).build();
        List<CallSchedule> expected = Arrays.asList(s1, s2);

        when(callScheduleMapper.getList(query)).thenReturn(expected);

        List<CallSchedule> result = callScheduleService.getList(query);

        assertEquals(2, result.size());
        assertEquals("work-hours", result.get(0).getName());
        assertEquals("work-weekend", result.get(1).getName());
        verify(callScheduleMapper).getList(query);
    }

    @Test
    @DisplayName("查询日程列表 - 空结果")
    void testGetListEmpty() {
        CallScheduleQuery query = new CallScheduleQuery();
        query.setName("nonexistent");

        when(callScheduleMapper.getList(query)).thenReturn(Collections.emptyList());

        List<CallSchedule> result = callScheduleService.getList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除日程 - 按单个 ID 逻辑删除")
    void testDeleteById() {
        CallScheduleQuery query = new CallScheduleQuery();
        query.setId(1L);

        callScheduleService.delete(query);

        verify(callScheduleService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除日程 - 按 ID 列表批量逻辑删除")
    void testDeleteByIds() {
        CallScheduleQuery query = new CallScheduleQuery();
        query.setIds(Arrays.asList(1L, 2L, 3L));

        callScheduleService.delete(query);

        verify(callScheduleService).updateBatchById(anyList());
    }

    @Test
    @DisplayName("删除日程 - 空的 query（无 id 无 ids）不执行删除")
    void testDeleteEmpty() {
        CallScheduleQuery query = new CallScheduleQuery();

        callScheduleService.delete(query);

        verify(callScheduleService, never()).updateBatchById(anyList());
    }
}
