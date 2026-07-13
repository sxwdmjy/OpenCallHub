package com.och.customer.application;

import com.och.customer.domain.CustomerPhone;
import com.och.customer.domain.CustomerProfile;
import com.och.customer.mapper.CustomerPhoneMapper;
import com.och.customer.mapper.CustomerPoolMapper;
import com.och.customer.mapper.CustomerProfileMapper;
import com.och.customer.service.impl.CustomerLookupServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CustomerLookupService 单元测试
 * <p>验证客户查询服务的增删改查及脱敏逻辑。</p>
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class CustomerLookupServiceTest {

    @Mock
    private CustomerProfileMapper profileMapper;

    @Mock
    private CustomerPhoneMapper phoneMapper;

    @Mock
    private CustomerPoolMapper poolMapper;

    @InjectMocks
    private CustomerLookupServiceImpl lookupService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lookupService, "baseMapper", profileMapper);
    }

    // ---------- findByPhone ----------

    @Test
    void findByPhone_shouldReturnProfile_whenPhoneExists() {
        // given
        String phone = "13800138000";
        CustomerProfile expected = profileWithId(1L);
        when(profileMapper.findByPhone(phone)).thenReturn(expected);

        // when
        CustomerProfile result = lookupService.findByPhone(phone);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(profileMapper).findByPhone(phone);
    }

    @Test
    void findByPhone_shouldReturnNull_whenPhoneNotFound() {
        // given
        String phone = "00000000000";
        when(profileMapper.findByPhone(phone)).thenReturn(null);

        // when
        CustomerProfile result = lookupService.findByPhone(phone);

        // then
        assertNull(result);
        verify(profileMapper).findByPhone(phone);
    }

    // ---------- getPhones ----------

    @Test
    void getPhones_shouldReturnPhoneList_whenCustomerHasPhones() {
        // given
        Long customerId = 1L;
        List<CustomerPhone> phones = Arrays.asList(
                phoneWithId(1L, customerId, "13800138000"),
                phoneWithId(2L, customerId, "13900139000")
        );
        when(phoneMapper.findByCustomerId(customerId)).thenReturn(phones);

        // when
        List<CustomerPhone> result = lookupService.getPhones(customerId);

        // then
        assertEquals(2, result.size());
        assertEquals("13800138000", result.get(0).getPhoneNumber());
        verify(phoneMapper).findByCustomerId(customerId);
    }

    @Test
    void getPhones_shouldReturnEmptyList_whenCustomerHasNoPhones() {
        // given
        Long customerId = 99L;
        when(phoneMapper.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // when
        List<CustomerPhone> result = lookupService.getPhones(customerId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(phoneMapper).findByCustomerId(customerId);
    }

    // ---------- addProfile ----------

    @Test
    void addProfile_shouldCallMapperInsert() {
        // given
        CustomerProfile profile = profileWithId(null);

        // when
        lookupService.addProfile(profile);

        // then
        verify(profileMapper).insert(profile);
    }

    // ---------- updateProfile ----------

    @Test
    void updateProfile_shouldCallMapperUpdateById() {
        // given
        CustomerProfile profile = profileWithId(1L);
        profile.setCustomerName("updated-name");

        // when
        lookupService.updateProfile(profile);

        // then
        verify(profileMapper).updateById(profile);
    }

    // ---------- getProfile ----------

    @Test
    void getProfile_shouldReturnProfile_whenIdExists() {
        // given
        Long id = 1L;
        CustomerProfile expected = profileWithId(id);
        when(profileMapper.selectById(id)).thenReturn(expected);

        // when
        CustomerProfile result = lookupService.getProfile(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(profileMapper).selectById(id);
    }

    @Test
    void getProfile_shouldReturnNull_whenIdNotFound() {
        // given
        Long id = 999L;
        when(profileMapper.selectById(id)).thenReturn(null);

        // when
        CustomerProfile result = lookupService.getProfile(id);

        // then
        assertNull(result);
        verify(profileMapper).selectById(id);
    }

    // ---------- deleteProfile ----------

    @Test
    void deleteProfile_shouldCallMapperDeleteById() {
        // given
        Long id = 1L;

        // when
        lookupService.deleteProfile(id);

        // then
        verify(profileMapper).deleteById(id);
    }

    // ---------- listProfiles ----------

    @Test
    void listProfiles_shouldReturnFilteredProfiles_whenQueryHasConditions() {
        // given
        CustomerProfile query = new CustomerProfile();
        query.setCustomerName("test");
        query.setStatus("active");
        List<CustomerProfile> expected = Collections.singletonList(profileWithId(1L));
        when(profileMapper.selectList(any())).thenReturn(expected);

        // when
        List<CustomerProfile> result = lookupService.listProfiles(query);

        // then
        assertEquals(1, result.size());
        verify(profileMapper).selectList(any());
    }

    @Test
    void listProfiles_shouldReturnAllProfiles_whenQueryIsNull() {
        // given
        List<CustomerProfile> expected = Arrays.asList(profileWithId(1L), profileWithId(2L));
        when(profileMapper.selectList(any())).thenReturn(expected);

        // when
        List<CustomerProfile> result = lookupService.listProfiles(null);

        // then
        assertEquals(2, result.size());
        verify(profileMapper).selectList(any());
    }

    @Test
    void listProfiles_shouldReturnEmptyList_whenNoProfilesMatch() {
        // given
        CustomerProfile query = new CustomerProfile();
        query.setCompanyName("nonexistent");
        when(profileMapper.selectList(any())).thenReturn(Collections.emptyList());

        // when
        List<CustomerProfile> result = lookupService.listProfiles(query);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(profileMapper).selectList(any());
    }

    // ---------- maskPhone (static) ----------

    @Test
    void maskPhone_shouldMaskMiddle4Digits_whenPhoneIs11Digits() {
        // given
        String phone = "13800138000";

        // when
        String masked = CustomerLookupServiceImpl.maskPhone(phone);

        // then
        assertEquals("138****8000", masked);
    }

    @Test
    void maskPhone_shouldReturnNull_whenPhoneIsNull() {
        // given
        String phone = null;

        // when
        String masked = CustomerLookupServiceImpl.maskPhone(phone);

        // then
        assertNull(masked);
    }

    @Test
    void maskPhone_shouldReturnOriginal_whenPhoneTooShort() {
        // given
        String phone = "12345";

        // when
        String masked = CustomerLookupServiceImpl.maskPhone(phone);

        // then
        assertEquals("12345", masked);
    }

    @Test
    void maskPhone_shouldReturnOriginal_whenPhoneIsEmpty() {
        // given
        String phone = "";

        // when
        String masked = CustomerLookupServiceImpl.maskPhone(phone);

        // then
        assertEquals("", masked);
    }

    // ---------- helpers ----------

    private CustomerProfile profileWithId(Long id) {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(id);
        profile.setCustomerName("customer-" + id);
        profile.setCompanyName("company-" + id);
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
