package com.och.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.och.notice.domain.NoticeMessage;
import com.och.notice.mapper.NoticeMessageMapper;
import com.och.notice.service.INoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知消息服务实现。
 * <p>
 * 管理通知消息的核心业务逻辑：查询、标记已读、发送、广播。
 * 继承 MyBatis-Plus ServiceImpl 获得批量操作和分页能力。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMessageMapper, NoticeMessage> implements INoticeService {

    @Override
    public List<NoticeMessage> myNotices(Long userId, boolean unreadOnly) {
        if (unreadOnly) {
            log.info("[NoticeService] 查询用户 {} 的未读通知列表", userId);
            return baseMapper.findUnreadByReceiver(userId);
        }
        log.info("[NoticeService] 查询用户 {} 的全部通知列表", userId);
        LambdaQueryWrapper<NoticeMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeMessage::getReceiverId, userId)
                .orderByDesc(NoticeMessage::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void markRead(Long id) {
        log.info("[NoticeService] 标记通知已读: id={}", id);
        NoticeMessage notice = new NoticeMessage();
        notice.setId(id);
        notice.setIsRead(true);
        notice.setReadTime(LocalDateTime.now());
        baseMapper.updateById(notice);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        log.info("[NoticeService] 标记用户 {} 的全部通知为已读", userId);
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<NoticeMessage> wrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        wrapper.eq(NoticeMessage::getReceiverId, userId)
                .eq(NoticeMessage::getIsRead, false)
                .set(NoticeMessage::getIsRead, true)
                .set(NoticeMessage::getReadTime, LocalDateTime.now());
        baseMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public NoticeMessage sendNotice(String title, String content, String type, Long senderId, Long receiverId) {
        log.info("[NoticeService] 发送通知: title={}, type={}, receiverId={}", title, type, receiverId);
        NoticeMessage notice = new NoticeMessage();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType(type != null ? type : "INFO");
        notice.setSenderId(senderId != null ? senderId : 0L);
        notice.setSenderName("系统");
        notice.setReceiverId(receiverId);
        notice.setIsRead(false);
        save(notice);
        return notice;
    }

    @Override
    @Transactional
    public int broadcast(String title, String content, String type, Long senderId, List<Long> receiverIds) {
        log.info("[NoticeService] 广播通知: title={}, receiverCount={}", title, receiverIds.size());
        List<NoticeMessage> notices = new ArrayList<>(receiverIds.size());
        for (Long receiverId : receiverIds) {
            NoticeMessage notice = new NoticeMessage();
            notice.setTitle(title);
            notice.setContent(content);
            notice.setType(type != null ? type : "INFO");
            notice.setSenderId(senderId != null ? senderId : 0L);
            notice.setSenderName("系统");
            notice.setReceiverId(receiverId);
            notice.setIsRead(false);
            notices.add(notice);
        }
        saveBatch(notices);
        return notices.size();
    }

    @Override
    public IPage<NoticeMessage> pageNotices(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<NoticeMessage> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(NoticeMessage::getTitle, keyword);
        }
        wrapper.orderByDesc(NoticeMessage::getCreatedAt);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
