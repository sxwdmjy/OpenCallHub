package com.och.calltask.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundCustomerState;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.calltask.service.impl.OutboundQueueServiceImpl;
import com.och.common.error.BizException;
import org.springframework.test.util.ReflectionTestUtils;
import com.och.common.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OutboundQueueService}.
 * <p>
 * Covers enqueue, batchEnqueue, dequeue, peek, queueSize, drainQueue, and removeFromQueue.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class OutboundQueueServiceTest {

    @Mock
    private OutboundCustomerMapper customerMapper;

    private OutboundQueueServiceImpl queueService;

    @BeforeEach
    void setUp() {
        queueService = new OutboundQueueServiceImpl();
        ReflectionTestUtils.setField(queueService, "baseMapper", customerMapper);
    }

    // ======================== enqueue ========================

    @Test
    void enqueue_shouldTransitionReadyToScheduled() {
        OutboundCustomer customer = buildCustomer(1L, 10L, OutboundCustomerState.READY);
        when(customerMapper.selectById(1L)).thenReturn(customer);

        OutboundCustomer result = queueService.enqueue(1L);

        assertThat(result.getState()).isEqualTo(OutboundCustomerState.SCHEDULED.name());
        verify(customerMapper).updateById(customer);
    }

    @Test
    void enqueue_shouldThrowBizExceptionWhenCustomerNotFound() {
        when(customerMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> queueService.enqueue(1L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.NOT_FOUND.getCode());
    }

    @Test
    void enqueue_shouldThrowBizExceptionWhenStateNotReady() {
        OutboundCustomer customer = buildCustomer(1L, 10L, OutboundCustomerState.DIALING);
        when(customerMapper.selectById(1L)).thenReturn(customer);

        assertThatThrownBy(() -> queueService.enqueue(1L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.BIZ_ILLEGAL_STATE.getCode());
    }

    // ======================== batchEnqueue ========================
    // Note: batchEnqueue's full LambdaUpdateWrapper flow requires MyBatis-Plus runtime
    // configuration and is better suited for integration tests. The guard-clause tests below
    // cover the null/empty input handling which is the only branch testable in isolation.

    @Test
    void batchEnqueue_shouldReturnZeroForEmptyList() {
        int count = queueService.batchEnqueue(10L, Collections.emptyList());

        assertThat(count).isEqualTo(0);
        verify(customerMapper, never()).update(any());
    }

    @Test
    void batchEnqueue_shouldReturnZeroForNullList() {
        int count = queueService.batchEnqueue(10L, null);

        assertThat(count).isEqualTo(0);
        verify(customerMapper, never()).update(any());
    }

    // ======================== dequeue ========================

    @Test
    void dequeue_shouldTransitionScheduledToDialing() {
        OutboundCustomer customer = buildCustomer(1L, 10L, OutboundCustomerState.SCHEDULED);
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(customer));

        OutboundCustomer result = queueService.dequeue(10L);

        assertThat(result.getState()).isEqualTo(OutboundCustomerState.DIALING.name());
        verify(customerMapper).updateById(customer);
    }

    @Test
    void dequeue_shouldReturnNullWhenQueueEmpty() {
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OutboundCustomer result = queueService.dequeue(10L);

        assertThat(result).isNull();
        verify(customerMapper, never()).updateById(any());
    }

    // ======================== peek ========================

    @Test
    void peek_shouldNotModifyState() {
        OutboundCustomer customer = buildCustomer(1L, 10L, OutboundCustomerState.SCHEDULED);
        when(customerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(customer);

        OutboundCustomer result = queueService.peek(10L);

        assertThat(result.getState()).isEqualTo(OutboundCustomerState.SCHEDULED.name());
        verify(customerMapper, never()).updateById(any());
    }

    @Test
    void peek_shouldReturnNullWhenQueueEmpty() {
        when(customerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        OutboundCustomer result = queueService.peek(10L);

        assertThat(result).isNull();
    }

    // ======================== queueSize ========================

    @Test
    void queueSize_shouldReturnScheduledCount() {
        when(customerMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        int size = queueService.queueSize(10L);

        assertThat(size).isEqualTo(5);
    }

    // ======================== removeFromQueue ========================

    @Test
    void removeFromQueue_shouldTransitionScheduledBackToReady() {
        OutboundCustomer customer = buildCustomer(1L, 10L, OutboundCustomerState.SCHEDULED);
        when(customerMapper.selectById(1L)).thenReturn(customer);

        queueService.removeFromQueue(1L);

        assertThat(customer.getState()).isEqualTo(OutboundCustomerState.READY.name());
        verify(customerMapper).updateById(customer);
    }

    @Test
    void removeFromQueue_shouldDoNothingWhenCustomerNotFound() {
        when(customerMapper.selectById(1L)).thenReturn(null);

        queueService.removeFromQueue(1L);

        verify(customerMapper, never()).updateById(any());
    }

    @Test
    void removeFromQueue_shouldDoNothingWhenStateNotScheduled() {
        OutboundCustomer customer = buildCustomer(1L, 10L, OutboundCustomerState.READY);
        when(customerMapper.selectById(1L)).thenReturn(customer);

        queueService.removeFromQueue(1L);

        verify(customerMapper, never()).updateById(any());
    }

    // ======================== drainQueue ========================

    @Test
    void drainQueue_shouldResetAllScheduledToReady() {
        OutboundCustomer c1 = buildCustomer(1L, 10L, OutboundCustomerState.SCHEDULED);
        OutboundCustomer c2 = buildCustomer(2L, 10L, OutboundCustomerState.SCHEDULED);
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(c1, c2));

        int drained = queueService.drainQueue(10L);

        assertThat(drained).isEqualTo(2);
        assertThat(c1.getState()).isEqualTo(OutboundCustomerState.READY.name());
        assertThat(c2.getState()).isEqualTo(OutboundCustomerState.READY.name());
        verify(customerMapper).updateById(c1);
        verify(customerMapper).updateById(c2);
    }

    @Test
    void drainQueue_shouldReturnZeroWhenQueueEmpty() {
        when(customerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        int drained = queueService.drainQueue(10L);

        assertThat(drained).isEqualTo(0);
        verify(customerMapper, never()).updateById(any());
    }

    // ======================== helper ========================

    private OutboundCustomer buildCustomer(Long id, Long taskId, OutboundCustomerState state) {
        return OutboundCustomer.builder()
                .id(id)
                .taskId(taskId)
                .phoneNumber("13800138000")
                .customerName("Customer-" + id)
                .state(state.name())
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
