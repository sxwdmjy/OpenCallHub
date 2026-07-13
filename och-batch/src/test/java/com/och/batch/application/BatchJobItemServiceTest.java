package com.och.batch.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.domain.ItemState;
import com.och.batch.mapper.BatchJobItemMapper;
import com.och.batch.service.impl.BatchJobItemServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchJobItemService 单元测试")
class BatchJobItemServiceTest {

    @Mock private BatchJobItemMapper itemMapper;
    @InjectMocks private BatchJobItemServiceImpl itemService;

    @BeforeEach
    void setUp() { ReflectionTestUtils.setField(itemService, "baseMapper", itemMapper); }

    @Test @DisplayName("listItems: 分页查询条目明细")
    void listItems_shouldReturnPaginatedItems() {
        BatchJobItem item = new BatchJobItem(); item.setId(1L); item.setJobId(1L);
        Page<BatchJobItem> page = new Page<>(1, 20); page.setRecords(List.of(item)); page.setTotal(1L);
        when(itemMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);
        assertThat(itemService.listItems(1L, 1, 20).getTotal()).isEqualTo(1L);
    }

    @Test @DisplayName("listFailedItems: 查询失败条目")
    void listFailedItems_shouldReturnFailedItems() {
        BatchJobItem item = new BatchJobItem(); item.setState("FAILED");
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        assertThat(itemService.listFailedItems(1L)).hasSize(1);
    }

    @Test @DisplayName("markSuccess: 条目标记为成功")
    void markSuccess_shouldSetStateAndProcessedAt() {
        itemService.markSuccess(1L);
        ArgumentCaptor<BatchJobItem> captor = ArgumentCaptor.forClass(BatchJobItem.class);
        verify(itemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo("SUCCESS");
    }

    @Test @DisplayName("markFailed: 条目标记为失败")
    void markFailed_shouldSetStateAndErrorMessage() {
        itemService.markFailed(1L, "err");
        ArgumentCaptor<BatchJobItem> captor = ArgumentCaptor.forClass(BatchJobItem.class);
        verify(itemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo("FAILED");
    }

    @Test @DisplayName("markRetry: 标记为待重试")
    void markRetry_shouldSetStateToFailedRetry() {
        itemService.markRetry(1L);
        ArgumentCaptor<BatchJobItem> captor = ArgumentCaptor.forClass(BatchJobItem.class);
        verify(itemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo("FAILED_RETRY");
    }

    @Test @DisplayName("countByState: 按状态统计")
    void countByState_shouldReturnCount() {
        when(itemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        assertThat(itemService.countByState(1L, ItemState.FAILED)).isEqualTo(5L);
    }
}
