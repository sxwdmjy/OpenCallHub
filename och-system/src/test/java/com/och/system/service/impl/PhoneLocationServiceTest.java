package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.utils.PhoneUtils;
import com.och.system.domain.entity.OchRegion;
import com.och.system.domain.entity.PhoneLocation;
import com.och.system.mapper.PhoneLocationMapper;
import com.och.system.service.IOchRegionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * PhoneLocationService 单元测试
 * <p>验证号码归属地查询服务的匹配与回退逻辑，包括携号转网场景。</p>
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class PhoneLocationServiceTest {

    @Mock
    private PhoneLocationMapper mapper;

    @Mock
    private IOchRegionService ochRegionService;

    private PhoneLocationServiceImpl service;

    private MockedStatic<PhoneUtils> phoneUtilsMock;

    @BeforeEach
    void setUp() throws Exception {
        service = new PhoneLocationServiceImpl(ochRegionService);
        setFieldInHierarchy(service, "baseMapper", mapper);
        phoneUtilsMock = mockStatic(PhoneUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (phoneUtilsMock != null) {
            phoneUtilsMock.close();
        }
    }

    /** Walk up the class hierarchy to find and set a declared field. */
    static void setFieldInHierarchy(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    // ===================== getLocation =====================

    @Test
    void getLocation_shouldReturnRegionName_whenPhoneFound() {
        String phone = "13800138000";
        phoneUtilsMock.when(() -> PhoneUtils.getPhoneArea(phone)).thenReturn("1380013");

        PhoneLocation phoneLocation = PhoneLocation.builder()
                .id(1)
                .phone("1380013")
                .areaCode("110000")
                .province("北京")
                .city("北京")
                .isp("移动")
                .ispType(1)
                .build();
        // ServiceImpl.getOne() calls baseMapper.selectOne(wrapper, true) — two args
        when(mapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(phoneLocation);

        OchRegion region = OchRegion.builder()
                .regionId("110000")
                .regionName("北京市")
                .mergerName("北京市")
                .build();
        when(ochRegionService.getOne(any(LambdaQueryWrapper.class))).thenReturn(region);

        String result = service.getLocation(phone);

        assertEquals("北京市", result);
    }

    @Test
    void getLocation_shouldReturnEmpty_whenPhoneAreaIsNull() {
        String phone = "00000000000";
        phoneUtilsMock.when(() -> PhoneUtils.getPhoneArea(phone)).thenReturn(null);

        String result = service.getLocation(phone);

        assertEquals("", result);
        verify(mapper, never()).selectOne(any(), anyBoolean());
        verify(ochRegionService, never()).getOne(any());
    }

    @Test
    void getLocation_shouldReturnEmpty_whenPhoneLocationNotFound() {
        String phone = "19900000000";
        phoneUtilsMock.when(() -> PhoneUtils.getPhoneArea(phone)).thenReturn("1990000");

        when(mapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);

        String result = service.getLocation(phone);

        assertEquals("", result);
        verify(ochRegionService, never()).getOne(any());
    }

    @Test
    void getLocation_shouldReturnEmpty_whenRegionNotFound() {
        String phone = "17700000000";
        phoneUtilsMock.when(() -> PhoneUtils.getPhoneArea(phone)).thenReturn("1770000");

        PhoneLocation phoneLocation = PhoneLocation.builder()
                .id(2)
                .phone("1770000")
                .areaCode("320000")
                .province("江苏")
                .city("南京")
                .isp("电信")
                .ispType(3)
                .build();
        when(mapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(phoneLocation);

        when(ochRegionService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        String result = service.getLocation(phone);

        assertEquals("", result);
    }
}
