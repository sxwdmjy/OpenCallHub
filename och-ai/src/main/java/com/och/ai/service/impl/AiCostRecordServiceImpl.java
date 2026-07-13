package com.och.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.och.ai.domain.AiCostRecord;
import com.och.ai.mapper.AiCostRecordMapper;
import com.och.ai.service.IAiCostRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 成本记录服务实现
 * <p>按调用明细持久化成本数据，后续可在此扩展租户、模型和插件维度的聚合统计。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Slf4j
@Service
public class AiCostRecordServiceImpl extends ServiceImpl<AiCostRecordMapper, AiCostRecord> implements IAiCostRecordService {

    @Override
    public List<AiCostRecord> getAllRecords() {
        return list();
    }

    @Override
    public Double getTotalCostByDate(LocalDate date) {
        return list(new LambdaQueryWrapper<AiCostRecord>()
                .eq(AiCostRecord::getRecordDate, date))
                .stream()
                .mapToDouble(record -> record.getCost() == null ? 0D : record.getCost())
                .sum();
    }

    @Override
    public void record(String callId, String sessionId, String pluginType, String pluginCode,
                       String modelName, int inputTokens, int outputTokens, double cost) {
        AiCostRecord record = new AiCostRecord();
        record.setCallId(callId);
        record.setSessionId(sessionId);
        record.setPluginType(pluginType);
        record.setPluginCode(pluginCode);
        record.setModelName(modelName);
        record.setInputTokens(inputTokens);
        record.setOutputTokens(outputTokens);
        record.setCost(cost);
        record.setRecordDate(LocalDate.now());
        save(record);
        log.info("[AiCostRecordService] 已记录AI调用成本: pluginType={}, pluginCode={}, cost={}", pluginType, pluginCode, cost);
    }
}
