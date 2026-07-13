package com.och.ai.service.impl;

import com.och.ai.domain.AiCostRecord;
import com.och.ai.service.IAiCostRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 调用成本追踪器
 * <p>
 * 作为应用层成本记录入口，内部委托成本记录服务持久化数据。
 * 调用方不需要关心成本数据最终来自数据库、缓存还是后续的报表聚合表。
 * </p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Service
@RequiredArgsConstructor
public class AiCostTrackerImpl {

    private final IAiCostRecordService aiCostRecordService;

    /**
     * 记录一次 AI 调用成本。
     * <p>只传递成本核算所需摘要，不记录 Prompt、密钥或供应商原始响应。</p>
     */
    public void record(String callId, String sessionId, String pluginType, String pluginCode,
                       String modelName, int inputTokens, int outputTokens, double cost) {
        aiCostRecordService.record(callId, sessionId, pluginType, pluginCode, modelName, inputTokens, outputTokens, cost);
    }

    /** 查询所有成本记录，供管理端分页能力落地前使用。 */
    public List<AiCostRecord> getAllRecords() {
        return aiCostRecordService.getAllRecords();
    }

    /** 按日期统计总费用。 */
    public Double getTotalCostByDate(LocalDate date) {
        return aiCostRecordService.getTotalCostByDate(date);
    }
}
