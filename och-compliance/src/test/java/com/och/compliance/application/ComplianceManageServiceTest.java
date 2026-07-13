package com.och.compliance.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.compliance.service.impl.ComplianceManageServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.compliance.domain.BlacklistRecord;
import com.och.compliance.domain.DncRecord;
import com.och.compliance.mapper.BlacklistRecordMapper;
import com.och.compliance.mapper.DncRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ComplianceManageService} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceManageService 单元测试")
class ComplianceManageServiceTest {

    @Mock
    private DncRecordMapper dncMapper;

    @Mock
    private BlacklistRecordMapper blacklistMapper;

    @InjectMocks
    private ComplianceManageServiceImpl manageService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() { org.springframework.test.util.ReflectionTestUtils.setField(manageService, "baseMapper", dncMapper); }

    // ======================== DNC ========================

    @Test
    @DisplayName("pageDnc: 分页查询DNC")
    void pageDnc_shouldReturnPaginatedResults() {
        DncRecord record = new DncRecord();
        record.setPhoneNumber("13800138000");
        Page<DncRecord> page = new Page<>(1, 20);
        page.setRecords(List.of(record));
        page.setTotal(1L);
        when(dncMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        IPage<DncRecord> result = manageService.pageDnc(1, 20, null);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("addDnc: 新增DNC号码")
    void addDnc_shouldInsertRecord() {
        when(dncMapper.insert(any())).thenReturn(1);

        DncRecord result = manageService.addDnc("13800138000", "投诉", "MANUAL", null);

        assertThat(result.getPhoneNumber()).isEqualTo("13800138000");
        assertThat(result.getReason()).isEqualTo("投诉");
        assertThat(result.getEnabled()).isTrue();
        verify(dncMapper).insert(result);
    }

    @Test
    @DisplayName("removeDnc: 删除DNC号码")
    void removeDnc_shouldDeleteById() {
        when(dncMapper.deleteById(1L)).thenReturn(1);

        manageService.removeDnc(1L);

        verify(dncMapper).deleteById(1L);
    }

    @Test
    @DisplayName("batchAddDnc: 批量添加DNC")
    void batchAddDnc_shouldInsertMultipleRecords() {
        DncRecord r1 = new DncRecord();
        r1.setPhoneNumber("13800138001");
        DncRecord r2 = new DncRecord();
        r2.setPhoneNumber("13800138002");
        when(dncMapper.insert(any())).thenReturn(1);

        int count = manageService.batchAddDnc(List.of(r1, r2));

        assertThat(count).isEqualTo(2);
        verify(dncMapper, times(2)).insert(any());
    }

    // ======================== Blacklist ========================

    @Test
    @DisplayName("pageBlacklist: 分页查询黑名单")
    void pageBlacklist_shouldReturnPaginatedResults() {
        BlacklistRecord record = new BlacklistRecord();
        record.setPhoneNumber("13900000000");
        Page<BlacklistRecord> page = new Page<>(1, 20);
        page.setRecords(List.of(record));
        page.setTotal(1L);
        when(blacklistMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        IPage<BlacklistRecord> result = manageService.pageBlacklist(1, 20, null);

        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("addBlacklist: 新增黑名单")
    void addBlacklist_shouldInsertRecord() {
        when(blacklistMapper.insert(any())).thenReturn(1);

        BlacklistRecord result = manageService.addBlacklist("13900000000", "诈骗", "HIGH");

        assertThat(result.getPhoneNumber()).isEqualTo("13900000000");
        assertThat(result.getLevel()).isEqualTo("HIGH");
        verify(blacklistMapper).insert(result);
    }

    @Test
    @DisplayName("batchAddBlacklist: 批量添加黑名单")
    void batchAddBlacklist_shouldInsertMultipleRecords() {
        BlacklistRecord r1 = new BlacklistRecord();
        r1.setPhoneNumber("13900000001");
        r1.setEnabled(null);
        when(blacklistMapper.insert(any())).thenReturn(1);

        int count = manageService.batchAddBlacklist(List.of(r1));

        assertThat(count).isEqualTo(1);
        // 验证 enabled 被设为 true
        ArgumentCaptor<BlacklistRecord> captor = ArgumentCaptor.forClass(BlacklistRecord.class);
        verify(blacklistMapper).insert(captor.capture());
        assertThat(captor.getValue().getEnabled()).isTrue();
    }
}
