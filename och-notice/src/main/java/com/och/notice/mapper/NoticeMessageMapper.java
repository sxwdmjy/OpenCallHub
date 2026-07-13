package com.och.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.notice.domain.NoticeMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知消息 Mapper，基于 MyBatis-Plus BaseMapper。
 * <p>
 * 提供 notice_message 表的基础 CRUD 操作，以及自定义 SQL 查询能力。
 * 自定义查询方法主要包括按用户 ID 查询未读通知列表等。
 * </p>
 *
 * @author danmo
 */
@Mapper
public interface NoticeMessageMapper extends BaseMapper<NoticeMessage> {

    /**
     * 查询指定用户的未读通知列表。
     * <p>
     * 根据接收者 ID 和已读状态查询通知，按创建时间倒序排列，
     * 确保最新的未读通知排在前面。
     * </p>
     *
     * @param receiverId 接收者用户 ID
     * @return 未读通知列表
     */
    @Select("SELECT * FROM notice_message WHERE receiver_id = #{receiverId} AND is_read = 0 " +
            "ORDER BY created_at DESC")
    List<NoticeMessage> findUnreadByReceiver(@Param("receiverId") Long receiverId);
}
