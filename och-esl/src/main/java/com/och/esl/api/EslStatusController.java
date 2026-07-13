package com.och.esl.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.esl.api.dto.EslConnectionStatus;
import com.och.esl.api.dto.NodeConnectionStatus;
import com.och.esl.application.EslCommandValidator;
import com.och.esl.application.EslConnectionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ESL 状态管理接口。
 * <p>提供 FreeSWITCH ESL 连接状态查询、健康检查和手动重连等管理功能。
 * 运维人员可通过此接口监控 ESL 连接健康状况。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>监控大屏展示 FreeSWITCH 连接状态</li>
 *   <li>故障排查：查看 ESL 连接是否正常</li>
 *   <li>运维操作：手动触发重连</li>
 * </ul>
 *
 * @author danmo
 */
@Tag(name = "ESL 连接管理", description = "ESL 连接状态查询、健康检查和手动重连/断开")
@RestController
@RequestMapping("/api/esl")
@RequiredArgsConstructor
public class EslStatusController {

    private final EslConnectionManager connectionManager;

    /**
     * ESL 连接健康检查。
     * <p>返回所有 FreeSWITCH 节点的连接状态汇总。</p>
     *
     * @return 各节点连接状态映射及汇总统计
     */
    @Operation(summary = "查询 ESL 连接状态", description = "返回所有 FreeSWITCH 节点的 ESL 连接状态汇总")
    @GetMapping("/status")
    public ApiResponse<EslConnectionStatus> status() {
        Map<String, EslConnectionManager.ConnectionState> rawConnections = connectionManager.getAllConnectionStates();
        Map<String, String> connections = rawConnections.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().name()));
        long connectedCount = rawConnections.values().stream()
                .filter(s -> s == EslConnectionManager.ConnectionState.CONNECTED).count();
        EslConnectionStatus result = new EslConnectionStatus(
                connections, connections.size(), connectedCount);
        return ApiResponse.success(result);
    }

    /**
     * 获取指定节点的连接状态。
     *
     * @param freeswitchId FreeSWITCH 节点标识
     * @return 节点连接状态
     */
    @Operation(summary = "查询节点连接状态", description = "获取指定 FreeSWITCH 节点的 ESL 连接状态详情")
    @GetMapping("/status/{freeswitchId}")
    public ApiResponse<NodeConnectionStatus> nodeStatus(@PathVariable String freeswitchId) {
        EslCommandValidator.validateFreeswitchId(freeswitchId);
        EslConnectionManager.ConnectionState state = connectionManager.getConnectionState(freeswitchId);
        NodeConnectionStatus result = new NodeConnectionStatus(
                freeswitchId, state.name(), connectionManager.isConnected(freeswitchId));
        return ApiResponse.success(result);
    }

    /**
     * 手动触发 ESL 重连。
     * <p>适用于 FreeSWITCH 维护后或网络故障恢复后的重连操作。</p>
     *
     * @param freeswitchId FreeSWITCH 节点标识
     * @return 操作结果
     */
    @Operation(summary = "手动触发 ESL 重连", description = "对指定 FreeSWITCH 节点发起 ESL 重连操作")
    @PostMapping("/reconnect/{freeswitchId}")
    public ApiResponse<String> reconnect(@PathVariable String freeswitchId) {
        EslCommandValidator.validateFreeswitchId(freeswitchId);
        connectionManager.reconnect(freeswitchId);
        return ApiResponse.success("重连指令已发送: " + freeswitchId);
    }

    /**
     * 手动断开 ESL 连接。
     *
     * @param freeswitchId FreeSWITCH 节点标识
     * @return 操作结果
     */
    @Operation(summary = "断开 ESL 连接", description = "手动断开指定 FreeSWITCH 节点的 ESL 连接")
    @PostMapping("/disconnect/{freeswitchId}")
    public ApiResponse<String> disconnect(@PathVariable String freeswitchId) {
        EslCommandValidator.validateFreeswitchId(freeswitchId);
        connectionManager.disconnect(freeswitchId);
        return ApiResponse.success("已断开连接: " + freeswitchId);
    }
}
