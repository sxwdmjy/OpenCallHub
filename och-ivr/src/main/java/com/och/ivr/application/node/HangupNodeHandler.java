package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 挂断节点处理器（策略模式）
 * <p>
 * 挂断当前通话，返回 HANGUP 终态标记。无下一节点。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class HangupNodeHandler implements IvrNodeHandler {

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        log.info("[HANGUP] 挂断: nodeId={}", currentNode.getNodeId());
        return IvrNodeHandlerResult.hangup(variables);
    }
}