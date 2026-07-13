package com.och.notice.api;

import com.och.common.api.ApiResponse;
import com.och.notice.service.INoticeService;
import com.och.notice.service.impl.NoticeServiceImpl;
import com.och.notice.domain.NoticeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * NoticeController 单元测试。
 * <p>
 * 使用 Mockito 模拟 NoticeService，验证各 REST 端点的调用链路和响应封装。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeController 单元测试")
class NoticeControllerTest {

    @Mock
    private INoticeService noticeService;

    @InjectMocks
    private NoticeController controller;

    private NoticeMessage notice1;
    private NoticeMessage notice2;

    @BeforeEach
    void setUp() {
        notice1 = new NoticeMessage();
        notice1.setId(1L);
        notice1.setTitle("系统通知");
        notice1.setContent("您有一条新的系统消息");
        notice1.setType("SYSTEM");
        notice1.setSenderId(0L);
        notice1.setSenderName("系统");
        notice1.setReceiverId(100L);
        notice1.setIsRead(false);
        notice1.setCreatedAt(LocalDateTime.of(2026, 6, 17, 10, 0));

        notice2 = new NoticeMessage();
        notice2.setId(2L);
        notice2.setTitle("告警通知");
        notice2.setContent("磁盘空间不足");
        notice2.setType("WARNING");
        notice2.setSenderId(0L);
        notice2.setSenderName("系统");
        notice2.setReceiverId(100L);
        notice2.setIsRead(true);
        notice2.setReadTime(LocalDateTime.of(2026, 6, 17, 9, 0));
        notice2.setCreatedAt(LocalDateTime.of(2026, 6, 17, 8, 0));
    }

    // ── GET /api/notice/my ────────────────────────────────────

    @Test
    @DisplayName("myNotices: 查询全部通知（unreadOnly=false），返回封装列表")
    void myNotices_shouldReturnAllNotices() {
        // given
        when(noticeService.myNotices(100L, false)).thenReturn(List.of(notice1, notice2));

        // when
        ApiResponse<List<NoticeMessage>> response = controller.myNotices(100L, false);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isNotNull().hasSize(2);
        assertThat(response.getData().get(0).getId()).isEqualTo(1L);
        assertThat(response.getData().get(0).getTitle()).isEqualTo("系统通知");
        assertThat(response.getData().get(1).getId()).isEqualTo(2L);
        assertThat(response.getData().get(1).getTitle()).isEqualTo("告警通知");

        verify(noticeService).myNotices(100L, false);
    }

    @Test
    @DisplayName("myNotices: 仅查询未读通知（unreadOnly=true），返回未读列表")
    void myNotices_shouldReturnUnreadOnly() {
        // given
        when(noticeService.myNotices(100L, true)).thenReturn(List.of(notice1));

        // when
        ApiResponse<List<NoticeMessage>> response = controller.myNotices(100L, true);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull().hasSize(1);
        assertThat(response.getData().get(0).getIsRead()).isFalse();

        verify(noticeService).myNotices(100L, true);
    }

    @Test
    @DisplayName("myNotices: 默认 unreadOnly=false 时返回全部通知")
    void myNotices_shouldDefaultToAllNotices() {
        // given
        when(noticeService.myNotices(100L, false)).thenReturn(List.of(notice1, notice2));

        // when
        ApiResponse<List<NoticeMessage>> response = controller.myNotices(100L, false);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(2);

        verify(noticeService).myNotices(100L, false);
    }

    @Test
    @DisplayName("myNotices: 空结果返回 code=200、空列表")
    void myNotices_shouldReturnEmptyList() {
        // given
        when(noticeService.myNotices(999L, false)).thenReturn(List.of());

        // when
        ApiResponse<List<NoticeMessage>> response = controller.myNotices(999L, false);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull().isEmpty();

        verify(noticeService).myNotices(999L, false);
    }

    // ── PUT /api/notice/{id}/read ─────────────────────────────

    @Test
    @DisplayName("markRead: 标记单条已读成功后返回空响应")
    void markRead_shouldReturnVoidSuccess() {
        // given
        doNothing().when(noticeService).markRead(1L);

        // when
        ApiResponse<Void> response = controller.markRead(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isNull();

        verify(noticeService).markRead(1L);
    }

    // ── PUT /api/notice/read-all ──────────────────────────────

    @Test
    @DisplayName("markAllRead: 批量已读成功后返回空响应")
    void markAllRead_shouldReturnVoidSuccess() {
        // given
        doNothing().when(noticeService).markAllRead(100L);

        // when
        ApiResponse<Void> response = controller.markAllRead(100L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isNull();

        verify(noticeService).markAllRead(100L);
    }

    // ── POST /api/notice/send ──────────────────────────────

    @Test
    @DisplayName("sendNotice: 发送通知返回创建的消息")
    void sendNotice_shouldReturnCreatedNotice() {
        NoticeMessage notice = new NoticeMessage();
        notice.setTitle("测试标题");
        notice.setContent("测试内容");
        notice.setType("INFO");
        notice.setReceiverId(1001L);
        when(noticeService.sendNotice("测试标题", "测试内容", "INFO", null, 1001L)).thenReturn(notice);

        ApiResponse<NoticeMessage> response = controller.sendNotice(notice);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getTitle()).isEqualTo("测试标题");
    }

    // ── POST /api/notice/broadcast ──────────────────────────────

    @Test
    @DisplayName("broadcast: 广播通知返回发送条数")
    void broadcast_shouldReturnCount() {
        NoticeMessage notice = new NoticeMessage();
        notice.setTitle("广播标题");
        notice.setContent("广播内容");
        List<Long> userIds = List.of(1001L, 1002L);
        when(noticeService.broadcast("广播标题", "广播内容", null, null, userIds)).thenReturn(2);

        ApiResponse<Integer> response = controller.broadcast(notice, userIds);

        assertThat(response.getData()).isEqualTo(2);
    }

    // ── GET /api/notice/page ──────────────────────────────

    @Test
    @DisplayName("pageNotices: 分页查询返回PageResponse")
    void pageNotices_shouldReturnPageResponse() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<NoticeMessage> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        page.setRecords(List.of());
        page.setTotal(0L);
        when(noticeService.pageNotices(1, 20, null)).thenReturn(page);

        com.och.common.api.ApiResponse<com.och.common.api.PageResponse<NoticeMessage>> response =
                controller.pageNotices(1, 20, null);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getTotal()).isEqualTo(0L);
    }
}
