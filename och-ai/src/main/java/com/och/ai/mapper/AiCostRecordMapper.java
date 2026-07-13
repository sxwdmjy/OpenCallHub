package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.AiCostRecord;

/**
 * AI 成本记录 Mapper
 * <p>提供 ai_cost_record 表的基础 CRUD 操作。
 * ai_cost_record 表记录 AI 服务调用的成本明细，包括调用类型、模型名称、
 * 输入/输出 Token 数量、费用金额和记录日期等信息，用于后续的成本统计与分析。</p>
 */
public interface AiCostRecordMapper extends BaseMapper<AiCostRecord> {

}
