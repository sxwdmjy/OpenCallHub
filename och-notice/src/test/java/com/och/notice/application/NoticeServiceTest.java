package com.och.notice.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.notice.domain.NoticeMessage;
import com.och.notice.mapper.NoticeMessageMapper;
import com.och.notice.service.impl.NoticeServiceImpl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NoticeService 单元测试。
 * <p>
 * 覆盖通知查询（全部/未读）和已读标记（单条/批量）的正常流程与边界情况。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeService 单元测试")
class NoticeServiceTest {

    @Mock
    private NoticeMessageMapper noticeMessageMapper;

    @Spy
    @InjectMocks
    private NoticeServiceImpl noticeService;

    private NoticeMessage notice1;
    private NoticeMessage notice2;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.addMapper(NoticeMessageMapper.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(noticeService, "baseMapper", noticeMessageMapper);
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

    // ── myNotices：查询全部通知 ───────────────────────────────

    @Test
    @DisplayName("myNotices: unreadOnly=false 时查询用户全部通知，按创建时间降序返回")
    void myNotices_shouldReturnAllNotices() {
        // given
        when(noticeMessageMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(notice1, notice2));

        // when
        List<NoticeMessage> result = noticeService.myNotices(100L, false);

        // then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("系统通知");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("告警通知");

        verify(noticeMessageMapper).selectList(any(LambdaQueryWrapper.class));
        verify(noticeMessageMapper, never()).findUnreadByReceiver(anyLong());
    }

    @Test
    @DisplayName("myNotices: unreadOnly=false 且无结果时返回空列表")
    void myNotices_shouldReturnEmptyListWhenNoNotices() {
        // given
        when(noticeMessageMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        // when
        List<NoticeMessage> result = noticeService.myNotices(999L, false);

        // then
        assertThat(result).isNotNull().isEmpty();
        verify(noticeMessageMapper).selectList(any(LambdaQueryWrapper.class));
    }

    // ── myNotices：查询未读通知 ───────────────────────────────

    @Test
    @DisplayName("myNotices: unreadOnly=true 时仅查询未读通知")
    void myNotices_shouldReturnUnreadOnly() {
        // given
        when(noticeMessageMapper.findUnreadByReceiver(100L)).thenReturn(List.of(notice1));

        // when
        List<NoticeMessage> result = noticeService.myNotices(100L, true);

        // then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getIsRead()).isFalse();

        verify(noticeMessageMapper).findUnreadByReceiver(100L);
        verify(noticeMessageMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("myNotices: unreadOnly=true 且无未读时返回空列表")
    void myNotices_shouldReturnEmptyWhenNoUnread() {
        // given
        when(noticeMessageMapper.findUnreadByReceiver(999L)).thenReturn(List.of());

        // when
        List<NoticeMessage> result = noticeService.myNotices(999L, true);

        // then
        assertThat(result).isNotNull().isEmpty();
        verify(noticeMessageMapper).findUnreadByReceiver(999L);
    }

    // ── markRead：标记单条已读 ────────────────────────────────

    @Test
    @DisplayName("markRead: 标记单条通知已读，设置 isRead=true 和 readTime")
    void markRead_shouldSetIsReadAndReadTime() {
        // given
        when(noticeMessageMapper.updateById(any(NoticeMessage.class))).thenReturn(1);

        // when
        noticeService.markRead(1L);

        // then
        ArgumentCaptor<NoticeMessage> captor = ArgumentCaptor.forClass(NoticeMessage.class);
        verify(noticeMessageMapper).updateById(captor.capture());
        NoticeMessage updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getIsRead()).isTrue();
        assertThat(updated.getReadTime()).isNotNull();
    }

    // ── markAllRead：标记全部已读 ─────────────────────────────

    @Test
    @DisplayName("markAllRead: 批量标记用户全部未读通知为已读")
    void markAllRead_shouldMarkAllUnreadForUser() {
        // given
        when(noticeMessageMapper.update(isNull(), any())).thenReturn(1);

        // when
        noticeService.markAllRead(100L);

        // then
        verify(noticeMessageMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("markAllRead: 用户无未读通知时 batch update 正常执行")
    void markAllRead_shouldSucceedWhenNoUnread() {
        // given
        when(noticeMessageMapper.update(isNull(), any())).thenReturn(0);

        // when
        noticeService.markAllRead(999L);

        // then
        verify(noticeMessageMapper).update(isNull(), any());
    }

    // ======================== sendNotice ========================

    @Test
    @DisplayName("sendNotice: 发送通知给指定用户")
    void sendNotice_shouldInsertAndReturnNotice() {
        when(noticeMessageMapper.insert(any())).thenReturn(1);

        NoticeMessage result = noticeService.sendNotice("标题", "内容", "INFO", 0L, 1001L);

        assertThat(result.getTitle()).isEqualTo("标题");
        assertThat(result.getReceiverId()).isEqualTo(1001L);
        assertThat(result.getIsRead()).isFalse();
        verify(noticeMessageMapper).insert(any());
    }

    @Test
    @DisplayName("sendNotice: type为null时默认INFO")
    void sendNotice_shouldDefaultTypeToInfoWhenNull() {
        when(noticeMessageMapper.insert(any())).thenReturn(1);

        NoticeMessage result = noticeService.sendNotice("标题", "内容", null, null, 1001L);

        assertThat(result.getType()).isEqualTo("INFO");
        assertThat(result.getSenderId()).isEqualTo(0L);
    }

    // ======================== broadcast ========================

    @Test
    @DisplayName("broadcast: 广播通知给多个用户")
    void broadcast_shouldInsertMultipleNotices() {
        doReturn(true).when(noticeService).saveBatch(any());

        int count = noticeService.broadcast("广播", "内容", "WARNING", 0L, Arrays.asList(1001L, 1002L, 1003L));

        assertThat(count).isEqualTo(3);
        verify(noticeService).saveBatch(any());
    }

    // ======================== pageNotices ========================

    @Test
    @DisplayName("pageNotices: 分页查询通知")
    void pageNotices_shouldReturnPaginatedResults() {
        NoticeMessage notice = new NoticeMessage();
        notice.setTitle("测试通知");
        Page<NoticeMessage> page = new Page<>(1, 20);
        page.setRecords(List.of(notice));
        page.setTotal(1L);
        when(noticeMessageMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

        IPage<NoticeMessage> result = noticeService.pageNotices(1, 20, null);

        assertThat(result.getTotal()).isEqualTo(1L);
    }
}
