package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 插件调用日志实体
 * <p>映射 plugin_call_log 表，记录每次 AI 插件（ASR/TTS/LLM）调用的完整信息：请求数据、响应数据、延迟、成功状态、错误信息和费用。</p>
 */
@Data
@TableName("plugin_call_log")
public class PluginCallLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private String callId;
    private String pluginType;
    private String pluginCode;
    private Integer turnNo;
    private String requestData;
    private String responseData;
    private Integer latencyMs;
    private Boolean success;
    private String errorMessage;
    private Double cost;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
