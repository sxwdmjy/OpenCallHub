package com.och.ticket.api;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.ticket.service.ITicketService;
import com.och.ticket.domain.Ticket;
import com.och.ticket.domain.TicketState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TicketController 单元测试。
 * <p>
 * 使用 Mockito 模拟 TicketService，验证各 REST 端点的调用链路和响应封装。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketController 单元测试")
class TicketControllerTest {

    @Mock
    private ITicketService ticketService;

    @InjectMocks
    private TicketController controller;

    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        sampleTicket = new Ticket();
        sampleTicket.setId(1L);
        sampleTicket.setTicketNo("WO-ABC12345");
        sampleTicket.setTitle("测试工单");
        sampleTicket.setDescription("描述内容");
        sampleTicket.setState(TicketState.NEW.name());
        sampleTicket.setPriority("HIGH");
        sampleTicket.setCustomerId(100L);
        sampleTicket.setCreatorId(200L);
        sampleTicket.setCreatedAt(LocalDateTime.now());
        sampleTicket.setUpdatedAt(LocalDateTime.now());
    }

    // ── GET /api/ticket ──────────────────────────────────────

    @Test
    @DisplayName("list: 分页查询返回工单列表")
    void list_shouldReturnPageResponse() {
        // given
        PageResponse<Ticket> page = PageResponse.of(
                List.of(sampleTicket), 1L, 1, 20);
        when(ticketService.list(1, 20)).thenReturn(page);

        // when
        ApiResponse<PageResponse<Ticket>> response = controller.list(1, 20);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getRecords()).hasSize(1);
        assertThat(response.getData().getTotal()).isEqualTo(1L);

        verify(ticketService).list(1, 20);
    }

    @Test
    @DisplayName("list: 默认参数查询")
    void list_shouldUseDefaultParams() {
        // given
        PageResponse<Ticket> page = PageResponse.of(
                Collections.emptyList(), 0L, 1, 20);
        when(ticketService.list(1, 20)).thenReturn(page);

        // when
        ApiResponse<PageResponse<Ticket>> response = controller.list(1, 20);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getRecords()).isEmpty();
        verify(ticketService).list(1, 20);
    }

    // ── GET /api/ticket/{id} ────────────────────────────────

    @Test
    @DisplayName("get: 查询存在的工单返回详情")
    void get_shouldReturnTicket() {
        // given
        when(ticketService.get(1L)).thenReturn(sampleTicket);

        // when
        ApiResponse<Ticket> response = controller.get(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
        assertThat(response.getData().getTicketNo()).isEqualTo("WO-ABC12345");
        assertThat(response.getData().getState()).isEqualTo(TicketState.NEW.name());

        verify(ticketService).get(1L);
    }

    @Test
    @DisplayName("get: 查询不存在的工单返回 null data")
    void get_shouldReturnNullDataWhenNotFound() {
        // given
        when(ticketService.get(999L)).thenReturn(null);

        // when
        ApiResponse<Ticket> response = controller.get(999L);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNull();

        verify(ticketService).get(999L);
    }

    // ── POST /api/ticket ────────────────────────────────────

    @Test
    @DisplayName("create: 创建工单返回含编号和 ID 的结果")
    void create_shouldReturnCreatedTicket() {
        // given
        Ticket input = new Ticket();
        input.setTitle("紧急工单");
        input.setDescription("系统不可用");
        input.setPriority("URGENT");

        Ticket created = new Ticket();
        created.setId(1L);
        created.setTicketNo("WO-XYZ98765");
        created.setTitle("紧急工单");
        created.setState(TicketState.NEW.name());

        when(ticketService.create(any(Ticket.class))).thenReturn(created);

        // when
        ApiResponse<Ticket> response = controller.create(input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getTicketNo()).isEqualTo("WO-XYZ98765");
        assertThat(response.getData().getState()).isEqualTo(TicketState.NEW.name());
        assertThat(response.getData().getTitle()).isEqualTo("紧急工单");

        verify(ticketService).create(input);
    }

    // ── PUT /api/ticket/{id} ────────────────────────────────

    @Test
    @DisplayName("update: 更新工单信息返回最新数据")
    void update_shouldReturnUpdatedTicket() {
        // given
        Ticket input = new Ticket();
        input.setTitle("更新后的标题");

        Ticket updated = new Ticket();
        updated.setId(1L);
        updated.setTitle("更新后的标题");
        updated.setState(TicketState.ASSIGNED.name());

        when(ticketService.update(eq(1L), any(Ticket.class))).thenReturn(updated);

        // when
        ApiResponse<Ticket> response = controller.update(1L, input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getTitle()).isEqualTo("更新后的标题");

        verify(ticketService).update(eq(1L), any(Ticket.class));
    }

    // ── PUT /api/ticket/{id}/assign ─────────────────────────

    @Test
    @DisplayName("assign: 分配工单成功后返回空响应")
    void assign_shouldReturnVoidSuccess() {
        // given
        doNothing().when(ticketService).assign(1L, 300L);

        // when
        ApiResponse<Void> response = controller.assign(1L, 300L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isNull();

        verify(ticketService).assign(1L, 300L);
    }

    // ── PUT /api/ticket/{id}/resolve ────────────────────────

    @Test
    @DisplayName("resolve: 解决工单成功后返回空响应")
    void resolve_shouldReturnVoidSuccess() {
        // given
        doNothing().when(ticketService).resolve(1L, "问题已修复，请确认");

        // when
        ApiResponse<Void> response = controller.resolve(1L, "问题已修复，请确认");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNull();

        verify(ticketService).resolve(1L, "问题已修复，请确认");
    }

    // ── PUT /api/ticket/{id}/close ──────────────────────────

    @Test
    @DisplayName("close: 关闭工单成功后返回空响应")
    void close_shouldReturnVoidSuccess() {
        // given
        doNothing().when(ticketService).close(1L);

        // when
        ApiResponse<Void> response = controller.close(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNull();

        verify(ticketService).close(1L);
    }
}
