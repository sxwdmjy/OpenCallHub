package com.och.esl.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.esl.api.dto.ChannelListResult;
import com.och.esl.api.dto.EslRuntimeStats;
import com.och.esl.application.ChannelStateTracker;
import com.och.esl.application.DtmfProcessingService;
import com.och.esl.application.EslCommandResultPublisher;
import com.och.esl.application.EslCommandValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ESL 运维管理接口。
 * <p>提供活跃通道查询、事件统计、命令调试等运维功能，供运维人员和监控系统使用。</p>
 *
 * @author danmo
 */
@Tag(name = "ESL 运维", description = "ESL 通道查询、运行时统计、DTMF 调试和命令清理")
@RestController
@RequestMapping("/api/esl/ops")
@RequiredArgsConstructor
public class EslOpsController {

    private final ChannelStateTracker channelTracker;
    private final DtmfProcessingService dtmfService;
    private final EslCommandResultPublisher commandPublisher;

    /**
     * 查询活跃通道列表（分页）。
     *
     * @param filter 可选过滤条件，按主叫或被叫号码模糊匹配
     * @param page   页码（从 0 开始），默认 0
     * @param size   每页大小，默认 50
     * @return 分页后的通道快照列表
     */
    @Operation(summary = "查询活跃通道列表", description = "分页返回当前活跃通道快照，支持按号码过滤")
    @GetMapping("/channels")
    public ApiResponse<ChannelListResult> listChannels(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<ChannelStateTracker.ChannelSnapshot> channels = channelTracker.getAllActive();

        // 可选过滤：按主叫号码或被叫号码模糊匹配
        if (filter != null && !filter.isEmpty()) {
            channels = channels.stream()
                    .filter(c -> contains(c.callerNumber(), filter) || contains(c.calledNumber(), filter))
                    .toList();
        }

        int total = channelTracker.activeCount();
        int filtered = channels.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, filtered);
        List<ChannelStateTracker.ChannelSnapshot> pageContent = (fromIndex < filtered)
                ? channels.subList(fromIndex, toIndex)
                : List.of();

        ChannelListResult result = new ChannelListResult(
                total, filtered, page, size, toIndex < filtered, pageContent);
        return ApiResponse.success(result);
    }

    /**
     * 查询单个通道详情。
     *
     * @param uuid FreeSWITCH 通道 UUID
     * @return 通道快照，不存在时返回 404
     */
    @Operation(summary = "查询通道详情", description = "根据 FreeSWITCH 通道 UUID 获取单个通道的详细状态")
    @GetMapping("/channels/{uuid}")
    public ApiResponse<ChannelStateTracker.ChannelSnapshot> channelDetail(@PathVariable String uuid) {
        EslCommandValidator.validateUuid(uuid);
        return channelTracker.findByUuid(uuid)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "通道不存在: " + uuid));
    }

    /**
     * ESL 运行时统计。
     *
     * @return 活跃通道数、待回执命令数、DTMF 缓冲区数
     */
    @Operation(summary = "查询运行时统计", description = "返回 ESL 运行时的核心指标")
    @GetMapping("/stats")
    public ApiResponse<EslRuntimeStats> stats() {
        EslRuntimeStats result = new EslRuntimeStats(
                channelTracker.activeCount(),
                commandPublisher.pendingCount(),
                dtmfService.bufferCount());
        return ApiResponse.success(result);
    }

    /**
     * 手动清空 DTMF 缓冲区（调试用）。
     *
     * @param callId 呼叫标识
     * @return 清空前的累积按键序列
     */
    @Operation(summary = "清空 DTMF 缓冲区", description = "手动清空指定 callId 的 DTMF 按键缓冲区")
    @PostMapping("/dtmf/flush/{callId}")
    public ApiResponse<String> flushDtmf(@PathVariable String callId) {
        EslCommandValidator.validateCallId(callId);
        String digits = dtmfService.flush(callId);
        return ApiResponse.success("已清空，累积按键=" + (digits.isEmpty() ? "(空)" : digits));
    }

    /**
     * 清理所有过期的 DTMF 缓冲区。
     *
     * @return 清理数量
     */
    @Operation(summary = "清理过期 DTMF 缓冲区", description = "清理所有超过 30 秒未操作的 DTMF 缓冲区")
    @PostMapping("/dtmf/clean-expired")
    public ApiResponse<String> cleanExpiredDtmf() {
        int removed = dtmfService.cleanExpired();
        return ApiResponse.success("已清理 " + removed + " 个过期缓冲区");
    }

    /**
     * 清理超时的待回执命令。
     *
     * @return 清理数量
     */
    @Operation(summary = "清理超时命令回执", description = "清理超过 120 秒未收到响应的待回执 ESL 命令")
    @PostMapping("/cmd/clean-expired")
    public ApiResponse<String> cleanExpiredCommands() {
        int removed = commandPublisher.cleanExpired(120);
        return ApiResponse.success("已清理 " + removed + " 个超时命令");
    }

    private boolean contains(String value, String filter) {
        return value != null && filter != null && value.contains(filter);
    }
}
