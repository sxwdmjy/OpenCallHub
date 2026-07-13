package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import com.och.ivr.domain.IvrNodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 菜单节点处理器（策略模式）
 * <p>
 * 根据用户DTMF按键匹配出边的条件表达式，选择对应的目标节点。
 * 精确匹配失败后走默认分支（条件为空的边）。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class MenuNodeHandler implements IvrNodeHandler {

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        if (edges == null || edges.isEmpty()) {
            return IvrNodeHandlerResult.end(variables);
        }

        // 1. 精确匹配用户按键
        for (IvrEdge edge : edges) {
            if (edge.getConditionExpr() != null && edge.getConditionExpr().equals(dtmfInput)) {
                log.debug("[MENU] DTMF匹配: nodeId={}, digit={}, target={}",
                        currentNode.getNodeId(), dtmfInput, edge.getTargetNodeId());
                return IvrNodeHandlerResult.continueTo(edge.getTargetNodeId(), variables);
            }
        }

        // 2. 无匹配时走默认分支
        for (IvrEdge edge : edges) {
            if (edge.getConditionExpr() == null || edge.getConditionExpr().isEmpty()) {
                log.debug("[MENU] 走默认分支: nodeId={}, digit={}, target={}",
                        currentNode.getNodeId(), dtmfInput, edge.getTargetNodeId());
                return IvrNodeHandlerResult.continueTo(edge.getTargetNodeId(), variables);
            }
        }

        log.warn("[MENU] 无匹配分支: nodeId={}, digit={}", currentNode.getNodeId(), dtmfInput);
        return IvrNodeHandlerResult.end(variables);
    }
}