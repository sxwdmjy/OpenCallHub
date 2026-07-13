package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrFlow;
import com.och.ivr.domain.IvrNode;
import com.och.ivr.domain.IvrNodeType;
import com.och.ivr.mapper.IvrEdgeMapper;
import com.och.ivr.mapper.IvrFlowMapper;
import com.och.ivr.mapper.IvrNodeMapper;
import com.och.ivr.service.IIvrFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * IVR流程服务实现
 * <p>
 * 提供 IVR 流程的 CRUD 和版本发布功能。发布前执行完整校验：
 * 检查开始节点、结束节点、孤立节点和无效连线。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IvrFlowServiceImpl implements IIvrFlowService {

    private final IvrFlowMapper ivrFlowMapper;
    private final IvrNodeMapper ivrNodeMapper;
    private final IvrEdgeMapper ivrEdgeMapper;

    @Override
    public List<IvrFlow> list() {
        return ivrFlowMapper.selectList(new LambdaQueryWrapper<IvrFlow>().orderByAsc(IvrFlow::getCreatedAt));
    }

    @Override
    public IvrFlow getById(Long id) {
        return ivrFlowMapper.selectById(id);
    }

    @Override
    public IvrFlow create(IvrFlow flow) {
        if (flow.getCurrentVersion() == null) flow.setCurrentVersion(1);
        if (flow.getEnabled() == null) flow.setEnabled(false);
        flow.setPublishedVersionId(null);
        if (ivrFlowMapper.insert(flow) <= 0) {
            throw new BizException(ErrorCode.BIZ_OPERATION_DENIED, "创建IVR流程失败");
        }
        log.info("[IVR流程] 创建成功: id={}, name={}", flow.getId(), flow.getFlowName());
        return flow;
    }

    @Override
    public IvrFlow update(Long id, IvrFlow flow) {
        IvrFlow existing = ivrFlowMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "IVR流程不存在");
        }
        flow.setId(id);
        ivrFlowMapper.updateById(flow);
        return ivrFlowMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        if (ivrFlowMapper.selectById(id) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "IVR流程不存在");
        }
        ivrFlowMapper.deleteById(id);
    }

    /**
     * 发布 IVR 流程版本
     * <p>
     * 发布前执行以下校验：
     * <ol>
     *   <li>流程必须有节点</li>
     *   <li>必须存在开始节点（START）</li>
     *   <li>必须存在结束节点（HANGUP 或仅有一条输入边无输出边的节点为终态）</li>
     *   <li>不能存在孤立节点（只有输出边无可达路径或完全无连接）</li>
     *   <li>不能存在无效连线（连向不存在节点的边）</li>
     * </ol>
     * 校验通过后，将当前版本标记为已发布并启用流程。
     * </p>
     */
    @Override
    public void publish(Long id) {
        IvrFlow existing = ivrFlowMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "IVR流程不存在");
        }

        long versionId = existing.getCurrentVersion();
        List<IvrNode> nodes = ivrNodeMapper.findByVersionId(versionId);
        List<IvrEdge> edges = ivrEdgeMapper.findByVersionId(versionId);

        // 校验
        validateFlowStructure(nodes, edges);

        // 发布
        IvrFlow update = new IvrFlow();
        update.setId(id);
        update.setPublishedVersionId(versionId);
        update.setEnabled(true);
        ivrFlowMapper.updateById(update);

        log.info("[IVR流程] 发布成功: id={}, version={}, nodes={}, edges={}",
                id, versionId, nodes.size(), edges.size());
    }

    @Override
    public void offline(Long id) {
        IvrFlow existing = ivrFlowMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "IVR流程不存在");
        }
        IvrFlow update = new IvrFlow();
        update.setId(id);
        update.setEnabled(false);
        ivrFlowMapper.updateById(update);
        log.info("[IVR流程] 下线成功: id={}", id);
    }

    /**
     * 校验 IVR 流程结构完整性
     */
    private void validateFlowStructure(List<IvrNode> nodes, List<IvrEdge> edges) {
        if (nodes.isEmpty()) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "IVR流程不包含任何节点，无法发布");
        }

        // 1. 必须有开始节点
        IvrNode startNode = findStartNode(nodes);
        if (startNode == null) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "IVR流程缺少开始节点（START）");
        }

        // 2. 必须有终态节点（HANGUP 或者没有出边的节点）
        boolean hasTerminal = nodes.stream().anyMatch(n ->
                IvrNodeType.HANGUP.name().equals(n.getNodeType())
                        || edges.stream().noneMatch(e -> e.getSourceNodeId().equals(n.getNodeId())));
        if (!hasTerminal) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "IVR流程缺少结束节点（所有路径必须有终止出口）");
        }

        // 3. 检查节点ID引用完整性（边的目标节点必须存在）
        Set<String> nodeIds = new HashSet<>();
        for (IvrNode n : nodes) nodeIds.add(n.getNodeId());
        for (IvrEdge e : edges) {
            if (!nodeIds.contains(e.getSourceNodeId())) {
                throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR,
                        "边引用了不存在的源节点: " + e.getSourceNodeId());
            }
            if (!nodeIds.contains(e.getTargetNodeId())) {
                throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR,
                        "边引用了不存在的目标节点: " + e.getTargetNodeId());
            }
        }

        // 4. 检查可达性：从 START 出发能到达所有节点
        Set<String> reachable = findReachableNodes(startNode.getNodeId(), edges);
        for (IvrNode n : nodes) {
            if (!reachable.contains(n.getNodeId())) {
                throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR,
                        "存在孤立节点（从START出发无法到达）: " + n.getNodeId());
            }
        }

        log.info("[IVR校验] 流程结构校验通过: nodes={}, edges={}", nodes.size(), edges.size());
    }

    /** 查找开始节点 */
    private IvrNode findStartNode(List<IvrNode> nodes) {
        for (IvrNode n : nodes) {
            if (IvrNodeType.START.name().equals(n.getNodeType())) return n;
        }
        return null;
    }

    /** BFS 搜索从指定节点可达的所有节点 */
    private Set<String> findReachableNodes(String startNodeId, List<IvrEdge> edges) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        visited.add(startNodeId);
        queue.add(startNodeId);

        Map<String, List<String>> adjacency = new HashMap<>();
        for (IvrEdge e : edges) {
            adjacency.computeIfAbsent(e.getSourceNodeId(), k -> new ArrayList<>()).add(e.getTargetNodeId());
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> neighbors = adjacency.getOrDefault(current, Collections.emptyList());
            for (String next : neighbors) {
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return visited;
    }
}