package com.och.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.notice.domain.NoticeMessage;

import java.util.List;

/**
 * 通知消息服务接口。
 */
public interface INoticeService {

    /** 查询指定用户的通知列表。 */
    List<NoticeMessage> myNotices(Long userId, boolean unreadOnly);

    /** 标记单条通知为已读。 */
    void markRead(Long id);

    /** 标记指定用户的所有未读通知为已读。 */
    void markAllRead(Long userId);

    /** 发送通知给指定用户。 */
    NoticeMessage sendNotice(String title, String content, String type, Long senderId, Long receiverId);

    /** 广播通知给多个用户。 */
    int broadcast(String title, String content, String type, Long senderId, List<Long> receiverIds);

    /** 管理员分页查询所有通知。 */
    IPage<NoticeMessage> pageNotices(int pageNum, int pageSize, String keyword);
}
