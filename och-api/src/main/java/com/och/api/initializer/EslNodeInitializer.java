package com.och.api.initializer;

import com.och.esl.application.EslConnectionManager;
import com.och.resource.domain.entity.FsNode;
import com.och.resource.service.IFsNodeService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ESL 节点初始化器
 * <p>在 Spring 容器启动后，从数据库 fs_node 表读取所有已启用的 FreeSWITCH 节点，
 * 自动建立 ESL 连接。管理员新增/启用节点后也可通过管理接口触发连接。</p>
 *
 * <h3>执行时机</h3>
 * <p>{@code @PostConstruct} 在依赖注入完成后、应用对外服务前执行。
 * 连接失败不会阻塞启动，仅记录错误日志。</p>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EslNodeInitializer {

    private final IFsNodeService fsNodeService;
    private final EslConnectionManager connectionManager;

    /**
     * 启动时自动加载已启用的 FreeSWITCH 节点并建立 ESL 连接
     */
    @PostConstruct
    public void initialize() {
        log.info("[EslNodeInitializer] 开始从数据库加载 FreeSWITCH 节点...");
        List<FsNode> nodes = fsNodeService.getEnabledNodes();

        if (nodes.isEmpty()) {
            log.warn("[EslNodeInitializer] 数据库中无已启用的 FreeSWITCH 节点，ESL 连接未建立");
            return;
        }

        for (FsNode node : nodes) {
            try {
                log.info("[EslNodeInitializer] 正在连接节点: nodeId={}, host={}:{}, name={}",
                        node.getNodeId(), node.getHost(), node.getPort(), node.getNodeName());
                connectionManager.connect(
                        node.getNodeId(),
                        node.getHost(),
                        node.getPort() != null ? node.getPort() : 8021,
                        node.getPassword()
                );
            } catch (Exception e) {
                log.error("[EslNodeInitializer] 节点连接失败: nodeId={}, host={}:{}",
                        node.getNodeId(), node.getHost(), node.getPort(), e);
            }
        }

        long connected = nodes.stream()
                .filter(n -> connectionManager.isConnected(n.getNodeId()))
                .count();
        log.info("[EslNodeInitializer] 初始化完成: 总节点={}, 已连接={}", nodes.size(), connected);
    }

    /**
     * 手动触发单个节点的连接（管理员操作后调用）
     *
     * @param node 已配置的 FreeSWITCH 节点信息
     */
    public void connectNode(FsNode node) {
        connectionManager.connect(
                node.getNodeId(),
                node.getHost(),
                node.getPort() != null ? node.getPort() : 8021,
                node.getPassword()
        );
    }
}
