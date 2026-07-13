package com.och.compliance.application;

import com.och.compliance.mapper.BlacklistRecordMapper;
import com.och.compliance.service.impl.ComplianceCheckServiceImpl;
import com.och.compliance.mapper.DncRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ComplianceCheckService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceCheckService 单元测试")
class ComplianceCheckServiceTest {

    @Mock
    private DncRecordMapper dncMapper;

    @Mock
    private BlacklistRecordMapper blacklistMapper;

    @InjectMocks
    private ComplianceCheckServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() { org.springframework.test.util.ReflectionTestUtils.setField(service, "baseMapper", dncMapper); }

    private static final String PHONE = "13800138000";

    // ======================== check(phoneNumber) ========================

    @Test
    @DisplayName("check: 号码在黑名单中 -> blocked=true, filterType=BLACKLIST")
    void shouldBlockWhenNumberInBlacklist() {
        // given: 仅 stub 黑名单，因为黑名单命中后短路，不会查 DNC
        when(blacklistMapper.isBlacklisted(eq(PHONE))).thenReturn(1);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check(PHONE);

        // then
        assertThat(result.blocked()).isTrue();
        assertThat(result.filterType()).isEqualTo("BLACKLIST");
        assertThat(result.reason()).contains(PHONE).contains("黑名单");
    }

    @Test
    @DisplayName("check: 号码在 DNC 列表中 -> blocked=true, filterType=DNC")
    void shouldBlockWhenNumberInDnc() {
        // given
        when(blacklistMapper.isBlacklisted(eq(PHONE))).thenReturn(0);
        when(dncMapper.isDncNumber(eq(PHONE))).thenReturn(1);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check(PHONE);

        // then
        assertThat(result.blocked()).isTrue();
        assertThat(result.filterType()).isEqualTo("DNC");
        assertThat(result.reason()).contains(PHONE).contains("DNC");
    }

    @Test
    @DisplayName("check: 号码同时在黑名单和 DNC -> 返回第一个匹配（黑名单）")
    void shouldReturnBlacklistWhenNumberInBothLists() {
        // given: 两个名单都有该号码，但黑名单先检查，短路返回
        when(blacklistMapper.isBlacklisted(eq(PHONE))).thenReturn(1);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check(PHONE);

        // then - 黑名单先检查，因此返回 BLACKLIST，dncMapper 不会被调用
        assertThat(result.blocked()).isTrue();
        assertThat(result.filterType()).isEqualTo("BLACKLIST");
    }

    @Test
    @DisplayName("check: 号码不在任何名单中 -> blocked=false")
    void shouldPassWhenNumberNotInAnyList() {
        // given
        when(blacklistMapper.isBlacklisted(eq(PHONE))).thenReturn(0);
        when(dncMapper.isDncNumber(eq(PHONE))).thenReturn(0);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check(PHONE);

        // then
        assertThat(result.blocked()).isFalse();
        assertThat(result.filterType()).isNull();
        assertThat(result.reason()).isNull();
    }

    @Test
    @DisplayName("check: 传入 null 号码 -> 兜底处理，不抛异常")
    void shouldHandleNullPhoneNumber() {
        // given - mock 对 null 参数的查询
        when(blacklistMapper.isBlacklisted(any())).thenReturn(0);
        when(dncMapper.isDncNumber(any())).thenReturn(0);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check(null);

        // then - 不抛异常，正常返回 pass（因为 count 不会 > 0）
        assertThat(result.blocked()).isFalse();
        assertThat(result.filterType()).isNull();
    }

    @Test
    @DisplayName("check: 传入空字符串号码 -> 兜底处理")
    void shouldHandleEmptyPhoneNumber() {
        // given
        when(blacklistMapper.isBlacklisted(eq(""))).thenReturn(0);
        when(dncMapper.isDncNumber(eq(""))).thenReturn(0);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check("");

        // then
        assertThat(result.blocked()).isFalse();
        assertThat(result.filterType()).isNull();
    }

    @Test
    @DisplayName("check: 传入空白字符串号码 -> 兜底处理")
    void shouldHandleBlankPhoneNumber() {
        // given
        String blankPhone = "   ";
        when(blacklistMapper.isBlacklisted(eq(blankPhone))).thenReturn(0);
        when(dncMapper.isDncNumber(eq(blankPhone))).thenReturn(0);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check(blankPhone);

        // then
        assertThat(result.blocked()).isFalse();
        assertThat(result.filterType()).isNull();
    }

    @Test
    @DisplayName("check: DNC 返回大于 1 的计数也视为命中")
    void shouldBlockWhenDncReturnsHighCount() {
        // given
        when(blacklistMapper.isBlacklisted(eq(PHONE))).thenReturn(0);
        when(dncMapper.isDncNumber(eq(PHONE))).thenReturn(3);

        // when
        ComplianceCheckServiceImpl.ComplianceResult result = service.check(PHONE);

        // then
        assertThat(result.blocked()).isTrue();
        assertThat(result.filterType()).isEqualTo("DNC");
    }

    // ======================== ComplianceResult 静态工厂方法 ========================

    @Test
    @DisplayName("ComplianceResult.pass(): blocked=false, reason 和 filterType 为 null")
    void complianceResultPassShouldReturnUnblocked() {
        // when
        ComplianceCheckServiceImpl.ComplianceResult result = ComplianceCheckServiceImpl.ComplianceResult.pass();

        // then
        assertThat(result.blocked()).isFalse();
        assertThat(result.reason()).isNull();
        assertThat(result.filterType()).isNull();
    }

    @Test
    @DisplayName("ComplianceResult.dnc(): blocked=true, filterType=DNC")
    void complianceResultDncShouldReturnBlockedWithDncType() {
        // when
        ComplianceCheckServiceImpl.ComplianceResult result =
                ComplianceCheckServiceImpl.ComplianceResult.dnc("测试原因");

        // then
        assertThat(result.blocked()).isTrue();
        assertThat(result.filterType()).isEqualTo("DNC");
        assertThat(result.reason()).isEqualTo("测试原因");
    }

    @Test
    @DisplayName("ComplianceResult.blacklist(): blocked=true, filterType=BLACKLIST")
    void complianceResultBlacklistShouldReturnBlockedWithBlacklistType() {
        // when
        ComplianceCheckServiceImpl.ComplianceResult result =
                ComplianceCheckServiceImpl.ComplianceResult.blacklist("测试原因");

        // then
        assertThat(result.blocked()).isTrue();
        assertThat(result.filterType()).isEqualTo("BLACKLIST");
        assertThat(result.reason()).isEqualTo("测试原因");
    }
}
