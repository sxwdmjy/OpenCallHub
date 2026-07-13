package com.och.customer.api;

import com.och.common.api.ApiResponse;
import com.och.common.trace.TraceContext;
import com.och.customer.service.ICustomerLookupService;
import com.och.customer.domain.CustomerPhone;
import com.och.customer.domain.CustomerProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CustomerLookupController 单元测试
 * <p>验证客户查询接口的请求处理逻辑。</p>
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class CustomerLookupControllerTest {

    @Mock
    private ICustomerLookupService lookupService;

    @InjectMocks
    private CustomerLookupController controller;

    @BeforeEach
    void setUp() {
        TraceContext.setTraceId("trace-test-001");
    }

    @AfterEach
    void tearDown() {
        TraceContext.remove();
    }

    // ---------- lookupByPhone ----------

    @Test
    @SuppressWarnings("unchecked")
    void lookupByPhone_shouldReturnCustomerAndMaskedPhones_whenCustomerFound() {
        // given
        String phone = "13800138000";
        CustomerProfile profile = profileWithId(1L, "张三", "测试公司");

        List<CustomerPhone> phones = Arrays.asList(
                phoneWithId(1L, 1L, "13800138000"),
                phoneWithId(2L, 1L, "13900139000")
        );

        when(lookupService.findByPhone(phone)).thenReturn(profile);
        when(lookupService.getPhones(profile.getId())).thenReturn(phones);

        // when
        ApiResponse<Map<String, Object>> response = controller.lookupByPhone(phone);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());

        Map<String, Object> data = response.getData();
        assertNotNull(data);
        assertEquals(true, data.get("found"));
        assertNotNull(data.get("customer"));
        assertNotNull(data.get("phones"));

        // verify phones are masked
        List<CustomerPhone> maskedPhones = (List<CustomerPhone>) data.get("phones");
        assertEquals(2, maskedPhones.size());
        assertEquals("138****8000", maskedPhones.get(0).getPhoneNumber());
        assertEquals("139****9000", maskedPhones.get(1).getPhoneNumber());

        verify(lookupService).findByPhone(phone);
        verify(lookupService).getPhones(profile.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupByPhone_shouldReturnNotFound_whenCustomerNotFound() {
        // given
        String phone = "00000000000";
        when(lookupService.findByPhone(phone)).thenReturn(null);

        // when
        ApiResponse<Map<String, Object>> response = controller.lookupByPhone(phone);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());

        Map<String, Object> data = response.getData();
        assertNotNull(data);
        assertEquals(false, data.get("found"));
        assertNull(data.get("customer"));

        verify(lookupService).findByPhone(phone);
        verify(lookupService, never()).getPhones(anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupByPhone_shouldHandleEmptyPhone() {
        // given
        String phone = "";
        when(lookupService.findByPhone(phone)).thenReturn(null);

        // when
        ApiResponse<Map<String, Object>> response = controller.lookupByPhone(phone);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());

        Map<String, Object> data = response.getData();
        assertEquals(false, data.get("found"));

        verify(lookupService).findByPhone(phone);
        verify(lookupService, never()).getPhones(anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupByPhone_shouldReturnCustomerWithEmptyPhones_whenCustomerHasNoPhones() {
        // given
        String phone = "13800138000";
        CustomerProfile profile = profileWithId(1L, "李四", "李四公司");

        when(lookupService.findByPhone(phone)).thenReturn(profile);
        when(lookupService.getPhones(profile.getId())).thenReturn(Collections.emptyList());

        // when
        ApiResponse<Map<String, Object>> response = controller.lookupByPhone(phone);

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());

        Map<String, Object> data = response.getData();
        assertEquals(true, data.get("found"));
        assertNotNull(data.get("customer"));

        List<CustomerPhone> phones = (List<CustomerPhone>) data.get("phones");
        assertNotNull(phones);
        assertTrue(phones.isEmpty());

        verify(lookupService).findByPhone(phone);
        verify(lookupService).getPhones(profile.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupByPhone_shouldHaveTraceIdInResponse() {
        // given
        String phone = "13800138000";
        CustomerProfile profile = profileWithId(1L, "王五", "王五公司");
        when(lookupService.findByPhone(phone)).thenReturn(profile);
        when(lookupService.getPhones(profile.getId())).thenReturn(Collections.emptyList());

        // when
        ApiResponse<Map<String, Object>> response = controller.lookupByPhone(phone);

        // then
        assertEquals("trace-test-001", response.getTraceId());
    }

    // ---------- helpers ----------

    private CustomerProfile profileWithId(Long id, String customerName, String companyName) {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(id);
        profile.setCustomerName(customerName);
        profile.setCompanyName(companyName);
        profile.setStatus("active");
        return profile;
    }

    private CustomerPhone phoneWithId(Long id, Long customerId, String phoneNumber) {
        CustomerPhone phone = new CustomerPhone();
        phone.setId(id);
        phone.setCustomerId(customerId);
        phone.setPhoneNumber(phoneNumber);
        phone.setPhoneType("MOBILE");
        phone.setIsPrimary(Boolean.TRUE);
        return phone;
    }
}
