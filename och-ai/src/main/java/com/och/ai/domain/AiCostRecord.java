package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 成本记录实体
 * <p>映射 ai_cost_record 表，按插件调用明细记录 Token 消耗、模型名称和费用，供后续成本报表聚合。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Data
@TableName("ai_cost_record")
public class AiCostRecord {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 呼叫ID */
    private String callId;

    /** AI 会话ID */
    private String sessionId;

    /** 插件类型：ASR/TTS/LLM/WORKFLOW */
    private String pluginType;

    /** 插件编码 */
    private String pluginCode;

    /** 模型名称 */
    private String modelName;

    /** 输入 Token 数 */
    private Integer inputTokens;

    /** 输出 Token 数 */
    private Integer outputTokens;

    /** 本次调用费用 */
    private Double cost;

    /** 成本归属日期 */
    private LocalDate recordDate;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
