package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.system.domain.entity.CallRoute;
import com.och.system.domain.query.route.CallRouteAddQuery;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.vo.route.CallRouteVo;
import com.och.system.mapper.CallRouteMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * CallRouteService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CallRouteService 单元测试")
class CallRouteServiceTest {

    @Mock
    private CallRouteMapper callRouteMapper;

    @Mock
    private ISysUserService sysUserService;

    private CallRouteServiceImpl callRouteService;

    @BeforeEach
    void setUp() throws Exception {
        callRouteService = new CallRouteServiceImpl(sysUserService);
        // Inject baseMapper into the parent ServiceImpl class
        Field baseMapperField = callRouteService.getClass()
                .getSuperclass().getSuperclass()
                .getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(callRouteService, callRouteMapper);
    }

    @Nested
    @DisplayName("add - 新增路由")
    class AddTests {

        @Test
        @DisplayName("should add route when name is unique")
        void shouldAddRouteWhenNameIsUnique() {
            // given
            CallRouteAddQuery query = new CallRouteAddQuery();
            query.setName("inbound-route");
            query.setRouteNum("021-10086");
            query.setType(1);
            query.setLevel(1);
            query.setRouteType(4);
            query.setRouteValue("1");

            when(callRouteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(callRouteMapper.insert(any(CallRoute.class))).thenReturn(1);

            // when
            callRouteService.add(query);

            // then
            ArgumentCaptor<CallRoute> captor = ArgumentCaptor.forClass(CallRoute.class);
            verify(callRouteMapper).insert(captor.capture());
            CallRoute saved = captor.getValue();
            assertEquals("inbound-route", saved.getName());
            assertEquals("021-10086", saved.getRouteNum());
            assertEquals(Integer.valueOf(1), saved.getType());
            assertEquals(Integer.valueOf(0), saved.getStatus());
        }

        @Test
        @DisplayName("should throw BizException when name is duplicate")
        void shouldThrowBizExceptionWhenNameIsDuplicate() {
            // given
            CallRouteAddQuery query = new CallRouteAddQuery();
            query.setName("duplicate-route");
            query.setRouteNum("021-10000");

            when(callRouteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // when & then
            BizException exception = assertThrows(BizException.class,
                    () -> callRouteService.add(query));
            assertEquals(ErrorCode.BIZ_NAME_DUPLICATE.getCode(), exception.getCode());
            verify(callRouteMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("getDetail - 查询路由详情")
    class GetDetailTests {

        @Test
        @DisplayName("should return route detail by id")
        void shouldReturnRouteDetailById() {
            // given
            Long routeId = 1L;
            CallRouteVo vo = new CallRouteVo();
            vo.setId(routeId);
            vo.setName("test-route");
            vo.setRouteNum("021-10086");
            vo.setType(1);
            vo.setStatus(0);

            when(callRouteMapper.getDetail(routeId)).thenReturn(vo);

            // when
            CallRouteVo result = callRouteService.getDetail(routeId);

            // then
            assertNotNull(result);
            assertEquals(routeId, result.getId());
            assertEquals("test-route", result.getName());
            assertEquals("021-10086", result.getRouteNum());
        }
    }

    @Nested
    @DisplayName("enable - 启用路由")
    class EnableTests {

        @Test
        @DisplayName("should enable a disabled route")
        void shouldEnableDisabledRoute() {
            // given
            Long routeId = 1L;
            CallRoute existingRoute = CallRoute.builder()
                    .id(routeId)
                    .name("test-route")
                    .status(0)
                    .build();

            when(callRouteMapper.selectById(routeId)).thenReturn(existingRoute);
            when(callRouteMapper.updateById(any(CallRoute.class))).thenReturn(1);

            // when
            callRouteService.enable(routeId);

            // then
            verify(callRouteMapper).updateById(argThat(route ->
                    route.getId().equals(routeId) && route.getStatus().equals(1)));
        }

        @Test
        @DisplayName("should throw BizException when route not found")
        void shouldThrowBizExceptionWhenNotFound() {
            // given
            Long routeId = 999L;
            when(callRouteMapper.selectById(routeId)).thenReturn(null);

            // when & then
            BizException exception = assertThrows(BizException.class,
                    () -> callRouteService.enable(routeId));
            assertEquals(ErrorCode.BIZ_RESOURCE_ID_REQUIRED.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("should throw BizException when route is already enabled")
        void shouldThrowBizExceptionWhenAlreadyEnabled() {
            // given
            Long routeId = 1L;
            CallRoute existingRoute = CallRoute.builder()
                    .id(routeId)
                    .status(1)
                    .build();

            when(callRouteMapper.selectById(routeId)).thenReturn(existingRoute);

            // when & then
            BizException exception = assertThrows(BizException.class,
                    () -> callRouteService.enable(routeId));
            assertEquals(ErrorCode.BIZ_OPERATION_DENIED.getCode(), exception.getCode());
            verify(callRouteMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("delete - 删除路由")
    class DeleteTests {

        @Test
        @DisplayName("should soft-delete a disabled route")
        void shouldSoftDeleteDisabledRoute() {
            // given
            CallRouteQuery query = new CallRouteQuery();
            query.setId(1L);

            CallRoute existingRoute = CallRoute.builder()
                    .id(1L)
                    .status(0)
                    .build();

            when(callRouteMapper.selectById(1L)).thenReturn(existingRoute);
            when(callRouteMapper.updateById(any(CallRoute.class))).thenReturn(1);

            // when
            callRouteService.delete(query);

            // then
            verify(callRouteMapper).updateById(argThat(route ->
                    route.getId().equals(1L)));
        }

        @Test
        @DisplayName("should throw BizException when trying to delete an enabled route")
        void shouldThrowBizExceptionWhenDeletingEnabledRoute() {
            // given
            CallRouteQuery query = new CallRouteQuery();
            query.setId(1L);

            CallRoute existingRoute = CallRoute.builder()
                    .id(1L)
                    .status(1)
                    .build();

            when(callRouteMapper.selectById(1L)).thenReturn(existingRoute);

            // when & then
            BizException exception = assertThrows(BizException.class,
                    () -> callRouteService.delete(query));
            assertEquals(ErrorCode.BIZ_OPERATION_DENIED.getCode(), exception.getCode());
            verify(callRouteMapper, never()).updateById(any());
        }
    }
}
