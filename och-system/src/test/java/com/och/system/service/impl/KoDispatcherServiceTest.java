package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.system.domain.entity.KoDispatcher;
import com.och.system.domain.query.dispatcher.KoDispatcherAddQuery;
import com.och.system.domain.query.dispatcher.KoDispatcherQuery;
import com.och.system.mapper.KoDispatcherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * KoDispatcherService 单元测试
 * <p>验证 Kamailio 调度器管理服务的增删改查逻辑。</p>
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class KoDispatcherServiceTest {

    @Mock
    private KoDispatcherMapper mapper;

    private KoDispatcherServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = spy(new KoDispatcherServiceImpl());
        setBaseMapper(service, mapper);
    }

    /**
     * Walk up the class hierarchy to find and set the {@code baseMapper} field
     * defined in MyBatis-Plus {@code ServiceImpl}.
     */
    static void setBaseMapper(Object target, Object mapper) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(target, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("baseMapper");
    }

    // ===================== add =====================

    @Test
    void add_shouldSaveKoDispatcherWithMappedFields() {
        KoDispatcherAddQuery query = new KoDispatcherAddQuery();
        query.setGroupId(10);
        query.setDestination("sip:192.168.1.100:5060");
        query.setFlags(0);
        query.setPriority(5);
        query.setAttrs("weight=50");
        query.setDescription("test-dispatcher");

        when(mapper.insert(any(KoDispatcher.class))).thenReturn(1);

        service.add(query);

        ArgumentCaptor<KoDispatcher> captor = ArgumentCaptor.forClass(KoDispatcher.class);
        verify(mapper).insert(captor.capture());
        KoDispatcher saved = captor.getValue();
        assertEquals(Integer.valueOf(10), saved.getSetid());
        assertEquals("sip:192.168.1.100:5060", saved.getDestination());
        assertEquals(Integer.valueOf(5), saved.getPriority());
        assertEquals("weight=50", saved.getAttrs());
    }

    @Test
    void add_shouldMapGroupIdToSetid() {
        KoDispatcherAddQuery query = new KoDispatcherAddQuery();
        query.setGroupId(99);
        query.setDestination("sip:10.0.0.1:5060");
        query.setFlags(1);

        when(mapper.insert(any(KoDispatcher.class))).thenReturn(1);

        service.add(query);

        ArgumentCaptor<KoDispatcher> captor = ArgumentCaptor.forClass(KoDispatcher.class);
        verify(mapper).insert(captor.capture());
        assertEquals(Integer.valueOf(99), captor.getValue().getSetid());
    }

    // ===================== edit =====================

    @Test
    void edit_shouldUpdateKoDispatcherById() {
        KoDispatcherAddQuery query = new KoDispatcherAddQuery();
        query.setId(1);
        query.setGroupId(20);
        query.setDestination("sip:192.168.2.100:5060");
        query.setStatus(1);

        when(mapper.updateById(any(KoDispatcher.class))).thenReturn(1);

        service.edit(query);

        ArgumentCaptor<KoDispatcher> captor = ArgumentCaptor.forClass(KoDispatcher.class);
        verify(mapper).updateById(captor.capture());
        KoDispatcher updated = captor.getValue();
        assertEquals(Integer.valueOf(1), updated.getId());
        assertEquals(Integer.valueOf(20), updated.getSetid());
        assertEquals(Integer.valueOf(1), updated.getStatus());
    }

    // ===================== delete =====================

    @Test
    void delete_shouldSoftDeleteById_whenIdProvided() {
        KoDispatcherQuery query = new KoDispatcherQuery();
        query.setId(5);

        // updateBatchById internally uses sqlSession — stub it on the spy
        doReturn(true).when(service).updateBatchById(anyList());

        service.delete(query);

        ArgumentCaptor<List<KoDispatcher>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).updateBatchById(captor.capture());
        List<KoDispatcher> list = captor.getValue();
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(5), list.get(0).getId());
        assertEquals(Integer.valueOf(1), list.get(0).getDelFlag());
    }

    @Test
    void delete_shouldSoftDeleteByIds_whenIdsProvided() {
        KoDispatcherQuery query = new KoDispatcherQuery();
        query.setIds(Arrays.asList(10, 20, 30));

        doReturn(true).when(service).updateBatchById(anyList());

        service.delete(query);

        ArgumentCaptor<List<KoDispatcher>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).updateBatchById(captor.capture());
        List<KoDispatcher> list = captor.getValue();
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(10), list.get(0).getId());
        assertEquals(Integer.valueOf(1), list.get(0).getDelFlag());
        assertEquals(Integer.valueOf(20), list.get(1).getId());
        assertEquals(Integer.valueOf(30), list.get(2).getId());
    }

    @Test
    void delete_shouldDoNothing_whenNoIdOrIds() {
        KoDispatcherQuery query = new KoDispatcherQuery();

        service.delete(query);

        verify(service, never()).updateBatchById(anyList());
    }

    // ===================== getDetail =====================

    @Test
    void getDetail_shouldReturnKoDispatcher_whenIdExists() {
        KoDispatcher dispatcher = KoDispatcher.builder()
                .id(1)
                .setid(10)
                .destination("sip:1.1.1.1:5060")
                .description("found")
                .build();
        when(mapper.selectById(1)).thenReturn(dispatcher);

        KoDispatcher result = service.getDetail(1);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getId());
        assertEquals("sip:1.1.1.1:5060", result.getDestination());
        verify(mapper).selectById(1);
    }

    @Test
    void getDetail_shouldReturnNull_whenIdNotFound() {
        when(mapper.selectById(999)).thenReturn(null);

        KoDispatcher result = service.getDetail(999);

        assertNull(result);
        verify(mapper).selectById(999);
    }

    // ===================== getList =====================

    @Test
    void getList_shouldReturnFilteredList_byGroupIdAndStatus() {
        KoDispatcherQuery query = new KoDispatcherQuery();
        query.setGroupId(10);
        query.setStatus(0);

        KoDispatcher d1 = KoDispatcher.builder().id(1).setid(10).status(0).build();
        KoDispatcher d2 = KoDispatcher.builder().id(2).setid(10).status(0).build();
        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(d1, d2));

        List<KoDispatcher> result = service.getList(query);

        assertEquals(2, result.size());
        verify(mapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void getList_shouldReturnEmptyList_whenNoMatches() {
        KoDispatcherQuery query = new KoDispatcherQuery();
        query.setStatus(99);

        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<KoDispatcher> result = service.getList(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mapper).selectList(any(LambdaQueryWrapper.class));
    }
}
