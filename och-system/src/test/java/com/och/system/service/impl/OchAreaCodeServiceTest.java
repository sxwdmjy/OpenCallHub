package com.och.system.service.impl;

import com.och.system.domain.entity.OchAreaCode;
import com.och.system.mapper.OchAreaCodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OchAreaCodeService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class OchAreaCodeServiceTest {

    @Mock
    private OchAreaCodeMapper ochAreaCodeMapper;

    @Spy
    private OchAreaCodeServiceImpl ochAreaCodeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ochAreaCodeService, "baseMapper", ochAreaCodeMapper);
    }

    // ==================== queryByCode (getById) ====================

    @Test
    @DisplayName("根据 ID 查询区号记录")
    void testQueryByCode() {
        OchAreaCode areaCode = OchAreaCode.builder()
                .id(1)
                .provincename("广东省")
                .cityname("广州市")
                .citycode("020")
                .cityadcode(440100)
                .build();

        when(ochAreaCodeMapper.selectById(1)).thenReturn(areaCode);

        OchAreaCode result = ochAreaCodeService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("广东省", result.getProvincename());
        assertEquals("广州市", result.getCityname());
        assertEquals("020", result.getCitycode());
        assertEquals(440100, result.getCityadcode());
        verify(ochAreaCodeMapper).selectById(1);
    }

    @Test
    @DisplayName("根据 ID 查询区号 - 记录不存在时返回 null")
    void testQueryByCodeNotFound() {
        when(ochAreaCodeMapper.selectById(999)).thenReturn(null);

        OchAreaCode result = ochAreaCodeService.getById(999);

        assertNull(result);
        verify(ochAreaCodeMapper).selectById(999);
    }

    // ==================== listCodes (list) ====================

    @Test
    @DisplayName("查询区号列表")
    void testListCodes() {
        OchAreaCode code1 = OchAreaCode.builder()
                .id(1).provincename("广东省").cityname("广州市").citycode("020").build();
        OchAreaCode code2 = OchAreaCode.builder()
                .id(2).provincename("广东省").cityname("深圳市").citycode("0755").build();
        OchAreaCode code3 = OchAreaCode.builder()
                .id(3).provincename("北京市").cityname("北京市").citycode("010").build();

        List<OchAreaCode> expected = Arrays.asList(code1, code2, code3);
        when(ochAreaCodeMapper.selectList(any())).thenReturn(expected);

        List<OchAreaCode> result = ochAreaCodeService.list();

        assertEquals(3, result.size());
        assertEquals("020", result.get(0).getCitycode());
        assertEquals("0755", result.get(1).getCitycode());
        assertEquals("010", result.get(2).getCitycode());
        verify(ochAreaCodeMapper).selectList(any());
    }

    @Test
    @DisplayName("查询区号列表 - 空结果")
    void testListCodesEmpty() {
        when(ochAreaCodeMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<OchAreaCode> result = ochAreaCodeService.list();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
