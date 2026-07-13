package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.system.domain.entity.CallEngine;
import com.och.system.domain.query.engine.CallEngineAddQuery;
import com.och.system.domain.query.engine.CallEngineQuery;
import com.och.system.mapper.CallEngineMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * CallEngineService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CallEngineService 单元测试")
class CallEngineServiceTest {

    @Mock
    private CallEngineMapper callEngineMapper;

    private CallEngineServiceImpl callEngineService;

    @BeforeEach
    void setUp() throws Exception {
        callEngineService = spy(new CallEngineServiceImpl());
        // Inject baseMapper into the parent ServiceImpl class from the external JAR
        Field baseMapperField = callEngineService.getClass()
                .getSuperclass().getSuperclass()
                .getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(callEngineService, callEngineMapper);
    }

    @Nested
    @DisplayName("addEngine - 新增引擎")
    class AddEngineTests {

        @Test
        @DisplayName("should save engine when name is unique")
        void shouldSaveEngineWhenNameIsUnique() {
            // given
            CallEngineAddQuery query = new CallEngineAddQuery();
            query.setName("test-engine");
            query.setProfile("asr-default");
            query.setType(1);
            query.setProviders(1);
            query.setTimbre("female");

            when(callEngineMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(callEngineMapper.insert(any(CallEngine.class))).thenReturn(1);

            // when
            callEngineService.addEngine(query);

            // then
            verify(callEngineMapper).selectList(any(LambdaQueryWrapper.class));
            ArgumentCaptor<CallEngine> captor = ArgumentCaptor.forClass(CallEngine.class);
            verify(callEngineMapper).insert(captor.capture());
            CallEngine saved = captor.getValue();
            assertEquals("test-engine", saved.getName());
            assertEquals("asr-default", saved.getProfile());
            assertEquals(Integer.valueOf(1), saved.getType());
            assertEquals(Integer.valueOf(1), saved.getProviders());
            assertEquals("female", saved.getTimbre());
        }

        @Test
        @DisplayName("should throw RuntimeException when engine name already exists")
        void shouldThrowExceptionWhenNameAlreadyExists() {
            // given
            CallEngineAddQuery query = new CallEngineAddQuery();
            query.setName("duplicate-engine");
            query.setProfile("tts-default");

            CallEngine existingEngine = CallEngine.builder()
                    .id(1L)
                    .name("duplicate-engine")
                    .build();

            when(callEngineMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(existingEngine));

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> callEngineService.addEngine(query));
            assertEquals("名称已存在", exception.getMessage());
            verify(callEngineMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("getDetail - 查询引擎详情")
    class GetDetailTests {

        @Test
        @DisplayName("should return engine by id")
        void shouldReturnEngineById() {
            // given
            Long engineId = 1L;
            CallEngine engine = CallEngine.builder()
                    .id(engineId)
                    .name("test-engine")
                    .profile("asr-default")
                    .type(1)
                    .providers(1)
                    .build();

            when(callEngineMapper.selectById(engineId)).thenReturn(engine);

            // when
            CallEngine result = callEngineService.getDetail(engineId);

            // then
            assertNotNull(result);
            assertEquals(engineId, result.getId());
            assertEquals("test-engine", result.getName());
            assertEquals("asr-default", result.getProfile());
        }

        @Test
        @DisplayName("should return null when engine not found")
        void shouldReturnNullWhenEngineNotFound() {
            // given
            Long engineId = 999L;
            when(callEngineMapper.selectById(engineId)).thenReturn(null);

            // when
            CallEngine result = callEngineService.getDetail(engineId);

            // then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getList - 查询引擎列表")
    class GetListTests {

        @Test
        @DisplayName("should return engine list by query")
        void shouldReturnEngineListByQuery() {
            // given
            CallEngineQuery query = new CallEngineQuery();
            query.setType(1);

            CallEngine engine1 = CallEngine.builder().id(1L).name("engine-1").type(1).build();
            CallEngine engine2 = CallEngine.builder().id(2L).name("engine-2").type(1).build();
            List<CallEngine> expectedList = List.of(engine1, engine2);

            when(callEngineMapper.getList(query)).thenReturn(expectedList);

            // when
            List<CallEngine> result = callEngineService.getList(query);

            // then
            assertEquals(2, result.size());
            assertEquals("engine-1", result.get(0).getName());
            assertEquals("engine-2", result.get(1).getName());
            verify(callEngineMapper).getList(query);
        }

        @Test
        @DisplayName("should return empty list when no engines match")
        void shouldReturnEmptyListWhenNoEnginesMatch() {
            // given
            CallEngineQuery query = new CallEngineQuery();
            query.setType(999);
            when(callEngineMapper.getList(query)).thenReturn(Collections.emptyList());

            // when
            List<CallEngine> result = callEngineService.getList(query);

            // then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("delete - 删除引擎")
    class DeleteTests {

        @Test
        @DisplayName("should soft-delete engine by id")
        void shouldSoftDeleteEngineById() {
            // given
            CallEngineQuery query = new CallEngineQuery();
            query.setId(1L);

            doReturn(true).when(callEngineService).updateBatchById(anyList());

            // when
            callEngineService.delete(query);

            // then
            verify(callEngineService).updateBatchById(argThat((List<CallEngine> list) ->
                    list.size() == 1 && list.get(0).getId().equals(1L)));
        }

        @Test
        @DisplayName("should soft-delete engines by id list")
        void shouldSoftDeleteEnginesByIdList() {
            // given
            CallEngineQuery query = new CallEngineQuery();
            query.setIds(List.of(1L, 2L, 3L));

            doReturn(true).when(callEngineService).updateBatchById(anyList());

            // when
            callEngineService.delete(query);

            // then
            verify(callEngineService).updateBatchById(argThat((List<CallEngine> list) ->
                    list.size() == 3));
        }

        @Test
        @DisplayName("should do nothing when no ids provided")
        void shouldDoNothingWhenNoIdsProvided() {
            // given
            CallEngineQuery query = new CallEngineQuery();

            // when
            callEngineService.delete(query);

            // then
            verify(callEngineService, never()).updateBatchById(anyList());
        }
    }

    @Nested
    @DisplayName("edit - 编辑引擎")
    class EditTests {

        @Test
        @DisplayName("should update engine when valid id provided")
        void shouldUpdateEngineWhenValidIdProvided() {
            // given
            CallEngineAddQuery query = new CallEngineAddQuery();
            query.setId(1L);
            query.setName("updated-engine");
            query.setProfile("updated-profile");
            query.setType(2);
            query.setProviders(2);

            CallEngine existingEngine = CallEngine.builder()
                    .id(1L)
                    .name("old-engine")
                    .profile("old-profile")
                    .type(1)
                    .providers(1)
                    .build();

            when(callEngineMapper.selectById(1L)).thenReturn(existingEngine);
            when(callEngineMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(callEngineMapper.updateById(any(CallEngine.class))).thenReturn(1);

            // when
            callEngineService.edit(query);

            // then
            verify(callEngineMapper).updateById(argThat(entity ->
                    "updated-engine".equals(entity.getName())
                            && "updated-profile".equals(entity.getProfile())
                            && Integer.valueOf(2).equals(entity.getType())
                            && Integer.valueOf(2).equals(entity.getProviders())));
        }

        @Test
        @DisplayName("should throw RuntimeException when engine id is invalid")
        void shouldThrowExceptionWhenEngineIdIsInvalid() {
            // given
            CallEngineAddQuery query = new CallEngineAddQuery();
            query.setId(999L);
            query.setName("some-engine");

            when(callEngineMapper.selectById(999L)).thenReturn(null);

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> callEngineService.edit(query));
            assertEquals("无效ID", exception.getMessage());
            verify(callEngineMapper, never()).updateById(any());
        }
    }
}
