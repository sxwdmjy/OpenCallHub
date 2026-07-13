package com.och.resource.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 音频素材实体，存储IVR、排队音、提示音等音频文件信息。
 * 支持多种音频格式，记录文件大小和播放时长。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("res_audio_asset")
public class AudioAsset {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetName;
    private String assetCode;
    private String fileKey;
    private Long fileSize;
    private Integer durationSec;
    private String formatType;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private Long updatedBy;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    private String status;
    private String remark;
}
