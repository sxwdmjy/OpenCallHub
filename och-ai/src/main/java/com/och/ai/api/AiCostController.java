package com.och.ai.api;

import com.och.ai.domain.AiCostRecord;
import com.och.ai.service.IAiCostRecordService;
import com.och.common.api.ApiResponse;
import com.och.common.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 调用成本报表
 * <p>查询 AI 插件的调用成本、Token 消耗和费用统计，用于后续成本核算与运营分析。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Tag(name = "AiCost", description = "AI调用成本统计")
@RestController
@RequestMapping("/api/ai/costs")
@RequiredArgsConstructor
public class AiCostController extends BaseController {

    private final IAiCostRecordService aiCostRecordService;

    @Operation(summary = "今日总费用")
    @GetMapping("/today")
    public ApiResponse<Double> getTodayCost() {
        return success(aiCostRecordService.getTotalCostByDate(LocalDate.now()));
    }

    @Operation(summary = "所有调用记录")
    @GetMapping("/records")
    public ApiResponse<List<AiCostRecord>> getRecords() {
        return success(aiCostRecordService.getAllRecords());
    }

    @Operation(summary = "手动记录一次调用成本")
    @PostMapping("/record")
    public ApiResponse<Void> record(@RequestParam String callId, @RequestParam String sessionId,
                                    @RequestParam String pluginType, @RequestParam String pluginCode,
                                    @RequestParam String modelName, @RequestParam int inputTokens,
                                    @RequestParam int outputTokens, @RequestParam double cost) {
        aiCostRecordService.record(callId, sessionId, pluginType, pluginCode, modelName, inputTokens, outputTokens, cost);
        return success();
    }
}
