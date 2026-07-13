package com.och.call.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 录音记录实体
 * <p>
 * 存储通话录音文件的元数据，包括录音URL、文件大小、时长、格式、可访问性等。
 * 与通话记录（CallRecord）多对一关联。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recording_record")
public class RecordingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long callRecordId;
    private String callId;
    private String recordingUrl;
    private Long fileSize;
    private Integer durationSeconds;
    private String format;
    private Boolean accessible;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
