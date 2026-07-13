package com.och.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.och.ai.domain.AiCostRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 成本记录服务接口
 * <p>负责 AI 插件调用成本明细的写入和统计，便于后续扩展日报、月报和租户级成本分析。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
public interface IAiCostRecordService extends IService<AiCostRecord> {

    /**
     * 查询全部成本记录。
     *
     * @return 成本记录列表
     */
    List<AiCostRecord> getAllRecords();

    /**
     * 按日期统计总费用。
     *
     * @param date 统计日期
     * @return 总费用
     */
    Double getTotalCostByDate(LocalDate date);

    /**
     * 记录一次 AI 插件调用成本。
     *
     * @param callId       呼叫 ID
     * @param sessionId    会话 ID
     * @param pluginType   插件类型
     * @param pluginCode   插件编码
     * @param modelName    模型名称
     * @param inputTokens  输入 Token 数
     * @param outputTokens 输出 Token 数
     * @param cost         调用费用
     */
    void record(String callId, String sessionId, String pluginType, String pluginCode,
                String modelName, int inputTokens, int outputTokens, double cost);
}
