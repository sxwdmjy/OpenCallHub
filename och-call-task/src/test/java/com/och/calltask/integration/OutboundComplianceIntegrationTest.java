package com.och.calltask.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundCustomerState;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.calltask.service.IOutboundFilterService;
import com.och.calltask.service.impl.OutboundFilterServiceImpl;
import com.och.compliance.service.IComplianceCheckService;
import com.och.compliance.service.impl.ComplianceCheckServiceImpl.ComplianceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 跨模块集成测试：外呼过滤 → 合规检查。
 * 验证 OutboundFilterService 正确调用 IComplianceCheckService，
 * DNC 黑名单拦截的客户被标记为 BLOCKED，合规客户变为 READY。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("外呼合规集成测试")
class OutboundComplianceIntegrationTest {

    @Mock private OutboundCustomerMapper customerMapper;
    @Mock private IComplianceCheckService complianceService;
    @InjectMocks private OutboundFilterServiceImpl filterService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filterService, "baseMapper", customerMapper);
    }

    @Test @DisplayName("黑名单客户被拦截标记为BLOCKED")
    void shouldBlockCustomerWhenInBlacklist() {
        OutboundCustomer blocked = customer(1L, "13800001111", OutboundCustomerState.IMPORTED);
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(blocked));
        when(complianceService.check(eq("13800001111")))
                .thenReturn(ComplianceResult.blacklist("黑名单号码"));

        filterService.applyFilters(100L);

        ArgumentCaptor<OutboundCustomer> captor = ArgumentCaptor.forClass(OutboundCustomer.class);
        verify(customerMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(OutboundCustomerState.BLOCKED.name());
        assertThat(captor.getValue().getFilterResult()).contains("BLACKLIST");
    }

    @Test @DisplayName("DNC号码被拦截标记为BLOCKED")
    void shouldBlockCustomerWhenInDnc() {
        OutboundCustomer blocked = customer(2L, "13800002222", OutboundCustomerState.IMPORTED);
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(blocked));
        when(complianceService.check(eq("13800002222")))
                .thenReturn(ComplianceResult.dnc("DNC号码"));

        filterService.applyFilters(100L);

        ArgumentCaptor<OutboundCustomer> captor = ArgumentCaptor.forClass(OutboundCustomer.class);
        verify(customerMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(OutboundCustomerState.BLOCKED.name());
        assertThat(captor.getValue().getFilterResult()).contains("DNC");
    }

    @Test @DisplayName("合规客户标记为READY可拨打")
    void shouldMarkCustomerReadyWhenCompliancePass() {
        OutboundCustomer ready = customer(3L, "13800003333", OutboundCustomerState.IMPORTED);
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ready));
        when(complianceService.check(eq("13800003333"))).thenReturn(ComplianceResult.pass());

        filterService.applyFilters(100L);

        ArgumentCaptor<OutboundCustomer> captor = ArgumentCaptor.forClass(OutboundCustomer.class);
        verify(customerMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(OutboundCustomerState.READY.name());
    }

    @Test @DisplayName("混合场景：部分拦截部分通过")
    void shouldHandleMixedResults() {
        OutboundCustomer c1 = customer(1L, "13800001111", OutboundCustomerState.IMPORTED);
        OutboundCustomer c2 = customer(2L, "13800002222", OutboundCustomerState.IMPORTED);
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1, c2));
        when(complianceService.check(eq("13800001111"))).thenReturn(ComplianceResult.blacklist("黑名单"));
        when(complianceService.check(eq("13800002222"))).thenReturn(ComplianceResult.pass());

        filterService.applyFilters(100L);

        verify(customerMapper, times(2)).updateById(any());
    }

    @Test @DisplayName("无导入客户时不调用合规检查")
    void shouldNotCallComplianceWhenNoCustomers() {
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        filterService.applyFilters(100L);

        verify(complianceService, never()).check(any());
    }

    private OutboundCustomer customer(Long id, String phone, OutboundCustomerState state) {
        OutboundCustomer c = new OutboundCustomer();
        c.setId(id);
        c.setTaskId(100L);
        c.setPhoneNumber(phone);
        c.setState(state.name());
        return c;
    }
}
