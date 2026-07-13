package com.och.ticket.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.ticket.domain.Ticket;
import com.och.ticket.domain.TicketState;
import com.och.ticket.mapper.TicketMapper;
import com.och.ticket.service.impl.TicketServiceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TicketService 单元测试。
 * <p>
 * 覆盖工单 CRUD 和状态流转（分配、解决、关闭）的正常流程与边界情况。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService 单元测试")
class TicketServiceTest {

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket existingTicket;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ticketService, "baseMapper", ticketMapper);
        existingTicket = new Ticket();
        existingTicket.setId(1L);
        existingTicket.setTicketNo("WO-ABC12345");
        existingTicket.setTitle("测试工单");
        existingTicket.setDescription("测试描述");
        existingTicket.setState(TicketState.NEW.name());
        existingTicket.setPriority("MEDIUM");
        existingTicket.setCustomerId(100L);
        existingTicket.setCreatorId(200L);
        existingTicket.setCreatedAt(LocalDateTime.now());
        existingTicket.setUpdatedAt(LocalDateTime.now());
    }

    // ── 分页查询 ──────────────────────────────────────────────

    @Test
    @DisplayName("list: 分页查询工单列表，按创建时间降序返回")
    void list_shouldReturnPageResponse() {
        // given
        Page<Ticket> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(existingTicket));
        mockPage.setTotal(1L);
        when(ticketMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<Ticket> response = ticketService.list(1, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(1L);
        assertThat(response.getPageNum()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getRecords().get(0).getTitle()).isEqualTo("测试工单");

        verify(ticketMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("list: 空结果分页查询返回空列表")
    void list_shouldReturnEmptyPage() {
        // given
        Page<Ticket> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0L);
        when(ticketMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<Ticket> response = ticketService.list(1, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).isEmpty();
        assertThat(response.getTotal()).isEqualTo(0L);
    }

    // ── 查询单个工单 ─────────────────────────────────────────

    @Test
    @DisplayName("get: 根据 ID 查询工单，返回工单实体")
    void get_shouldReturnTicket() {
        // given
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);

        // when
        Ticket result = ticketService.get(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTicketNo()).isEqualTo("WO-ABC12345");
        assertThat(result.getState()).isEqualTo(TicketState.NEW.name());

        verify(ticketMapper).selectById(1L);
    }

    @Test
    @DisplayName("get: 查询不存在的工单，返回 null")
    void get_shouldReturnNullWhenNotFound() {
        // given
        when(ticketMapper.selectById(999L)).thenReturn(null);

        // when
        Ticket result = ticketService.get(999L);

        // then
        assertThat(result).isNull();
        verify(ticketMapper).selectById(999L);
    }

    // ── 创建工单 ─────────────────────────────────────────────

    @Test
    @DisplayName("create: 创建工单，自动生成编号并设为 NEW 状态")
    void create_shouldGenerateTicketNoAndSetNewState() {
        // given
        Ticket input = new Ticket();
        input.setTitle("新工单");
        input.setDescription("新工单描述");
        input.setPriority("HIGH");
        input.setCustomerId(100L);
        input.setCreatorId(200L);

        when(ticketMapper.insert(any(Ticket.class))).thenReturn(1);

        // when
        Ticket result = ticketService.create(input);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicketNo()).isNotNull();
        assertThat(result.getTicketNo()).startsWith("WO-");
        assertThat(result.getTicketNo()).hasSize(11); // "WO-" + 8 uppercase chars
        assertThat(result.getState()).isEqualTo(TicketState.NEW.name());
        assertThat(result.getTitle()).isEqualTo("新工单");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).insert(captor.capture());
        Ticket inserted = captor.getValue();
        assertThat(inserted.getState()).isEqualTo(TicketState.NEW.name());
        assertThat(inserted.getTicketNo()).isNotNull();
    }

    // ── 更新工单 ─────────────────────────────────────────────

    @Test
    @DisplayName("update: 根据 ID 更新工单信息并返回最新数据")
    void update_shouldUpdateAndReturnLatest() {
        // given
        Ticket updatedInput = new Ticket();
        updatedInput.setTitle("更新后的标题");
        updatedInput.setDescription("更新后的描述");

        Ticket afterUpdate = new Ticket();
        afterUpdate.setId(1L);
        afterUpdate.setTicketNo("WO-ABC12345");
        afterUpdate.setTitle("更新后的标题");
        afterUpdate.setDescription("更新后的描述");
        afterUpdate.setState(TicketState.NEW.name());

        when(ticketMapper.updateById(any(Ticket.class))).thenReturn(1);
        when(ticketMapper.selectById(1L)).thenReturn(afterUpdate);

        // when
        Ticket result = ticketService.update(1L, updatedInput);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("更新后的标题");
        assertThat(result.getDescription()).isEqualTo("更新后的描述");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());
        Ticket toUpdate = captor.getValue();
        assertThat(toUpdate.getId()).isEqualTo(1L);
        assertThat(toUpdate.getTitle()).isEqualTo("更新后的标题");

        verify(ticketMapper).selectById(1L);
    }

    // ── 状态流转：分配 ──────────────────────────────────────

    @Test
    @DisplayName("assign: NEW 状态工单分配成功，状态变更为 ASSIGNED")
    void assign_shouldChangeNewToAssigned() {
        // given
        existingTicket.setState(TicketState.NEW.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);
        when(ticketMapper.updateById(any(Ticket.class))).thenReturn(1);

        // when
        ticketService.assign(1L, 300L);

        // then
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());
        Ticket updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getAssigneeId()).isEqualTo(300L);
        assertThat(updated.getState()).isEqualTo(TicketState.ASSIGNED.name());

        verify(ticketMapper).selectById(1L);
    }

    @Test
    @DisplayName("assign: REOPENED 状态工单分配成功，状态变更为 ASSIGNED")
    void assign_shouldChangeReopenedToAssigned() {
        // given
        existingTicket.setState(TicketState.REOPENED.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);
        when(ticketMapper.updateById(any(Ticket.class))).thenReturn(1);

        // when
        ticketService.assign(1L, 300L);

        // then
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(TicketState.ASSIGNED.name());
    }

    @Test
    @DisplayName("assign: 工单不存在时抛出 IllegalArgumentException")
    void assign_shouldThrowWhenTicketNotFound() {
        // given
        when(ticketMapper.selectById(999L)).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> ticketService.assign(999L, 300L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("工单不存在")
                .hasMessageContaining("999");

        verify(ticketMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("assign: ASSIGNED 状态工单不可再分配，抛出 IllegalStateException")
    void assign_shouldThrowWhenAlreadyAssigned() {
        // given
        existingTicket.setState(TicketState.ASSIGNED.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);

        // when / then
        assertThatThrownBy(() -> ticketService.assign(1L, 300L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("当前工单状态为")
                .hasMessageContaining(TicketState.ASSIGNED.name());

        verify(ticketMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("assign: CLOSED 状态工单不可分配，抛出 IllegalStateException")
    void assign_shouldThrowWhenTicketClosed() {
        // given
        existingTicket.setState(TicketState.CLOSED.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);

        // when / then
        assertThatThrownBy(() -> ticketService.assign(1L, 300L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("当前工单状态为");

        verify(ticketMapper, never()).updateById(any());
    }

    // ── 状态流转：解决 ──────────────────────────────────────

    @Test
    @DisplayName("resolve: ASSIGNED 状态工单解决成功，状态变更为 RESOLVED")
    void resolve_shouldChangeAssignedToResolved() {
        // given
        existingTicket.setState(TicketState.ASSIGNED.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);
        when(ticketMapper.updateById(any(Ticket.class))).thenReturn(1);

        // when
        ticketService.resolve(1L, "问题已修复");

        // then
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());
        Ticket updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getState()).isEqualTo(TicketState.RESOLVED.name());
        assertThat(updated.getRemark()).isEqualTo("问题已修复");
        assertThat(updated.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("resolve: PROCESSING 状态工单解决成功，状态变更为 RESOLVED")
    void resolve_shouldChangeProcessingToResolved() {
        // given
        existingTicket.setState(TicketState.PROCESSING.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);
        when(ticketMapper.updateById(any(Ticket.class))).thenReturn(1);

        // when
        ticketService.resolve(1L, "处理完成");

        // then
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(TicketState.RESOLVED.name());
    }

    @Test
    @DisplayName("resolve: 工单不存在时抛出 IllegalArgumentException")
    void resolve_shouldThrowWhenTicketNotFound() {
        // given
        when(ticketMapper.selectById(999L)).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> ticketService.resolve(999L, "备注"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("工单不存在");

        verify(ticketMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("resolve: NEW 状态工单不可解决，抛出 IllegalStateException")
    void resolve_shouldThrowWhenTicketIsNew() {
        // given
        existingTicket.setState(TicketState.NEW.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);

        // when / then
        assertThatThrownBy(() -> ticketService.resolve(1L, "备注"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("当前工单状态为")
                .hasMessageContaining(TicketState.NEW.name());

        verify(ticketMapper, never()).updateById(any());
    }

    // ── 状态流转：关闭 ──────────────────────────────────────

    @Test
    @DisplayName("close: RESOLVED 状态工单关闭成功，状态变更为 CLOSED")
    void close_shouldChangeResolvedToClosed() {
        // given
        existingTicket.setState(TicketState.RESOLVED.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);
        when(ticketMapper.updateById(any(Ticket.class))).thenReturn(1);

        // when
        ticketService.close(1L);

        // then
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());
        Ticket updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getState()).isEqualTo(TicketState.CLOSED.name());
    }

    @Test
    @DisplayName("close: 工单不存在时抛出 IllegalArgumentException")
    void close_shouldThrowWhenTicketNotFound() {
        // given
        when(ticketMapper.selectById(999L)).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> ticketService.close(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("工单不存在");

        verify(ticketMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("close: 已关闭的工单重复关闭，抛出 IllegalStateException")
    void close_shouldThrowWhenAlreadyClosed() {
        // given
        existingTicket.setState(TicketState.CLOSED.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);

        // when / then
        assertThatThrownBy(() -> ticketService.close(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("工单已关闭");

        verify(ticketMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("close: New 状态工单可直接关闭")
    void close_shouldChangeNewToClosed() {
        // given
        existingTicket.setState(TicketState.NEW.name());
        when(ticketMapper.selectById(1L)).thenReturn(existingTicket);
        when(ticketMapper.updateById(any(Ticket.class))).thenReturn(1);

        // when
        ticketService.close(1L);

        // then
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(TicketState.CLOSED.name());
    }
}
