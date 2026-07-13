package com.och.system.service.impl;

import com.och.system.domain.entity.CallDisplay;
import com.och.system.domain.query.display.CallDisplayAddQuery;
import com.och.system.domain.query.display.CallDisplayQuery;
import com.och.system.domain.vo.display.CallDisplayVo;
import com.och.system.mapper.CallDisplayMapper;
import com.och.system.service.IPhoneLocationService;
import com.och.system.service.ISysUserService;
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
import static org.mockito.Mockito.*;

/**
 * CallDisplayService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CallDisplayService 单元测试")
class CallDisplayServiceTest {

    @Mock
    private CallDisplayMapper callDisplayMapper;

    @Mock
    private ISysUserService sysUserService;

    @Mock
    private IPhoneLocationService phoneLocationService;

    private CallDisplayServiceImpl callDisplayService;

    @BeforeEach
    void setUp() throws Exception {
        callDisplayService = spy(new CallDisplayServiceImpl(sysUserService, phoneLocationService));
        // Inject baseMapper into the parent ServiceImpl class
        Field baseMapperField = callDisplayService.getClass()
                .getSuperclass().getSuperclass()
                .getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(callDisplayService, callDisplayMapper);
    }

    @Nested
    @DisplayName("add - 新增号码")
    class AddTests {

        @Test
        @DisplayName("should add display with phone location")
        void shouldAddDisplayWithPhoneLocation() {
            // given
            CallDisplayAddQuery query = new CallDisplayAddQuery();
            query.setPhone("13800138000");
            query.setType(1);

            when(callDisplayMapper.getList(any(CallDisplayQuery.class)))
                    .thenReturn(Collections.emptyList());
            when(phoneLocationService.getLocation("13800138000"))
                    .thenReturn("广东深圳");
            when(callDisplayMapper.insert(any(CallDisplay.class))).thenReturn(1);

            // when
            callDisplayService.add(query);

            // then
            ArgumentCaptor<CallDisplay> captor = ArgumentCaptor.forClass(CallDisplay.class);
            verify(callDisplayMapper).insert(captor.capture());
            CallDisplay saved = captor.getValue();
            assertEquals("13800138000", saved.getPhone());
            assertEquals(Integer.valueOf(1), saved.getType());
            assertEquals("广东深圳", saved.getArea());
            verify(phoneLocationService).getLocation("13800138000");
        }

        @Test
        @DisplayName("should throw RuntimeException when phone already exists")
        void shouldThrowExceptionWhenPhoneAlreadyExists() {
            // given
            CallDisplayAddQuery query = new CallDisplayAddQuery();
            query.setPhone("13800138000");

            CallDisplayVo existingVo = new CallDisplayVo();
            existingVo.setId(1L);
            existingVo.setPhone("13800138000");

            when(callDisplayMapper.getList(any(CallDisplayQuery.class)))
                    .thenReturn(List.of(existingVo));

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> callDisplayService.add(query));
            assertEquals("号码已存在", exception.getMessage());
            verify(callDisplayMapper, never()).insert(any());
            verify(phoneLocationService, never()).getLocation(any());
        }
    }

    @Nested
    @DisplayName("getDetail - 查询号码详情")
    class GetDetailTests {

        @Test
        @DisplayName("should return call display detail by id")
        void shouldReturnDetailById() {
            // given
            Long displayId = 1L;
            CallDisplay display = CallDisplay.builder()
                    .id(displayId)
                    .phone("13800138000")
                    .type(1)
                    .area("广东深圳")
                    .build();

            when(callDisplayMapper.selectById(displayId)).thenReturn(display);

            // when
            CallDisplayVo result = callDisplayService.getDetail(displayId);

            // then
            assertNotNull(result);
            assertEquals("13800138000", result.getPhone());
            assertEquals(Integer.valueOf(1), result.getType());
            assertEquals("广东深圳", result.getArea());
        }
    }

    @Nested
    @DisplayName("getList - 查询号码列表")
    class GetListTests {

        @Test
        @DisplayName("should return display list by query")
        void shouldReturnDisplayListByQuery() {
            // given
            CallDisplayQuery query = new CallDisplayQuery();
            query.setType(1);

            CallDisplayVo vo1 = new CallDisplayVo();
            vo1.setId(1L);
            vo1.setPhone("13800138000");
            vo1.setType(1);

            CallDisplayVo vo2 = new CallDisplayVo();
            vo2.setId(2L);
            vo2.setPhone("13900139000");
            vo2.setType(1);

            List<CallDisplayVo> expectedList = List.of(vo1, vo2);

            when(callDisplayMapper.getList(query)).thenReturn(expectedList);

            // when
            List<CallDisplayVo> result = callDisplayService.getList(query);

            // then
            assertEquals(2, result.size());
            assertEquals("13800138000", result.get(0).getPhone());
            assertEquals("13900139000", result.get(1).getPhone());
            verify(callDisplayMapper).getList(query);
        }
    }

    @Nested
    @DisplayName("delete - 删除号码")
    class DeleteTests {

        @Test
        @DisplayName("should soft-delete display by id")
        void shouldSoftDeleteById() {
            // given
            CallDisplayQuery query = new CallDisplayQuery();
            query.setId(1L);

            doReturn(true).when(callDisplayService).updateBatchById(anyList());

            // when
            callDisplayService.delete(query);

            // then
            verify(callDisplayService).updateBatchById(argThat((List<CallDisplay> list) ->
                    list.size() == 1 && list.get(0).getId().equals(1L)));
        }

        @Test
        @DisplayName("should do nothing when no id or idList provided")
        void shouldDoNothingWhenNoIdsProvided() {
            // given
            CallDisplayQuery query = new CallDisplayQuery();

            // when
            callDisplayService.delete(query);

            // then
            verify(callDisplayService, never()).updateBatchById(anyList());
        }
    }
}
