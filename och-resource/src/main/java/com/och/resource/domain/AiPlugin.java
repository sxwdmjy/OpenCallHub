package com.och.resource.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI插件实体，记录接入的AI引擎配置信息。
 * 包括插件类型、提供商、API端点和认证信息等。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("res_ai_plugin")
public class AiPlugin {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String pluginName;
    private String pluginType;
    private String provider;
    private String endpointUrl;
    private String apiKey;
    private String apiSecret;
    private String configJson;
    private Boolean enabled;
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
