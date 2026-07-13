package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 条件判断节点处理器（策略模式）
 * <p>
 * 根据上下文变量值匹配出边的条件表达式，选择第一个满足条件的分支。
 * 支持 ==（等于）和 !=（不等于）两种操作符。无匹配时走默认分支。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class ConditionNodeHandler implements IvrNodeHandler {

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        if (edges == null || edges.isEmpty()) {
            return IvrNodeHandlerResult.end(variables);
        }

        // 1. 遍历条件分支
        for (IvrEdge edge : edges) {
            String cond = edge.getConditionExpr();
            if (cond == null || cond.isEmpty()) continue;

            if (evaluateCondition(cond, variables)) {
                log.debug("[CONDITION] 条件匹配: cond={}, target={}", cond, edge.getTargetNodeId());
                return IvrNodeHandlerResult.continueTo(edge.getTargetNodeId(), variables);
            }
        }

        // 2. 无精确匹配时走默认分支
        for (IvrEdge edge : edges) {
            if (edge.getConditionExpr() == null || edge.getConditionExpr().isEmpty()) {
                log.debug("[CONDITION] 默认分支: target={}", edge.getTargetNodeId());
                return IvrNodeHandlerResult.continueTo(edge.getTargetNodeId(), variables);
            }
        }

        log.warn("[CONDITION] 无匹配分支: nodeId={}", currentNode.getNodeId());
        return IvrNodeHandlerResult.end(variables);
    }

    /**
     * 对单个条件表达式求值
     * <p>
     * 支持格式：varName==value（等于）、varName!=value（不等于）、
     * 或直接传变量名（检查变量是否为非空非"false"的真值）。
     * </p>
     */
    private boolean evaluateCondition(String condition, Map<String, Object> variables) {
        if (condition == null || variables == null) return false;

        if (condition.contains("!=")) {
            String[] parts = condition.split("!=", 2);
            return !String.valueOf(variables.get(parts[0].trim())).equals(parts[1].trim());
        }

        if (condition.contains("==")) {
            String[] parts = condition.split("==", 2);
            return parts[1].trim().equals(String.valueOf(variables.get(parts[0].trim())));
        }

        // 简单布尔判断
        Object actual = variables.get(condition.trim());
        return actual != null && !"false".equals(String.valueOf(actual));
    }
}