package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 默认节点处理器（策略模式）
 * <p>
 * 处理 START、COLLECT_DIGITS、API_CALL、TRANSFER_AGENT、TRANSFER_SKILL_GROUP、TRANSFER_VOICEMAIL
 * 等不需要特殊路由逻辑的节点类型，默认沿第一条出边继续。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class DefaultNodeHandler implements IvrNodeHandler {

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        String nextNodeId = (edges != null && !edges.isEmpty()) ? edges.get(0).getTargetNodeId() : null;

        if (nextNodeId != null) {
            log.debug("[DEFAULT] 节点类型={}，默认路由: nodeId={}, nextNode={}",
                    currentNode.getNodeType(), currentNode.getNodeId(), nextNodeId);
            return IvrNodeHandlerResult.continueTo(nextNodeId, variables);
        }

        log.info("[DEFAULT] 默认路由无下一节点，流程结束: nodeId={}, nodeType={}",
                currentNode.getNodeId(), currentNode.getNodeType());
        return IvrNodeHandlerResult.end(variables);
    }
}