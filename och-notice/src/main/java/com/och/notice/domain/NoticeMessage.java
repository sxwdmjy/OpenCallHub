package com.och.notice.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 通知消息 */
@Data @TableName("notice_message")
public class NoticeMessage {
    @TableId(type = IdType.AUTO) private Long id;
    private String title;              // 标题
    private String content;            // 内容
    private String type;               // 类型: SYSTEM/WARNING/INFO
    private Long senderId;             // 发送者(系统=0)
    private String senderName;
    private Long receiverId;           // 接收者
    private Boolean isRead;            // 已读
    private LocalDateTime readTime;    // 读取时间
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
