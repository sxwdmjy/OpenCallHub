package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.error.BizException;
import com.och.system.domain.entity.SysOperLog;
import com.och.system.domain.query.BaseQuery;
import com.och.system.domain.query.log.SysOperationLogQuery;
import com.och.system.mapper.SysOperLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysOperLogServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class SysOperLogServiceTest {

    @Mock
    private SysOperLogMapper sysOperLogMapper;

    @Spy
    @InjectMocks
    private SysOperLogServiceImpl sysOperLogService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sysOperLogService, "baseMapper", sysOperLogMapper);
    }

    // ── getList ───────────────────────────────────────────────────────────

    @Test
    void getList_shouldReturnLogList() {
        SysOperationLogQuery query = new SysOperationLogQuery();
        SysOperLog log1 = SysOperLog.builder().id(1L).title("Login").status(0).build();
        SysOperLog log2 = SysOperLog.builder().id(2L).title("Logout").status(0).build();

        when(sysOperLogMapper.getList(query)).thenReturn(List.of(log1, log2));

        List<SysOperLog> result = sysOperLogService.getList(query);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SysOperLog::getTitle).containsExactly("Login", "Logout");
        verify(sysOperLogMapper).getList(query);
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    void delete_shouldSoftDeleteByIds() {
        SysOperationLogQuery query = new SysOperationLogQuery();
        query.setIds(List.of(1L, 2L, 3L));

        doReturn(true).when(sysOperLogService).updateBatchById(anyList(), eq(200));

        sysOperLogService.delete(query);

        verify(sysOperLogService).updateBatchById(anyList(), eq(200));
    }

    @Test
    void delete_shouldThrowBizException_whenIdsEmpty() {
        SysOperationLogQuery query = new SysOperationLogQuery();
        query.setIds(List.of());

        assertThatThrownBy(() -> sysOperLogService.delete(query))
                .isInstanceOf(BizException.class);

        verify(sysOperLogService, never()).updateBatchById(anyList(), anyInt());
    }

    // ── emptyOperation ────────────────────────────────────────────────────

    @Test
    void emptyOperation_shouldRemoveAllLogs() {
        doReturn(1).when(sysOperLogMapper).delete(any(LambdaQueryWrapper.class));

        BaseQuery query = new BaseQuery();
        sysOperLogService.emptyOperation(query);

        verify(sysOperLogMapper).delete(any(LambdaQueryWrapper.class));
    }
}
