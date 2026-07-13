package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 播放语音节点处理器（策略模式）
 * <p>
 * 播放语音后沿第一条出边继续流转（单一路由，无分支判断）。
 * 适用于 PLAY 和 PLAYBACK 两种节点类型。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class PlayNodeHandler implements IvrNodeHandler {

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        String nextNodeId = (edges != null && !edges.isEmpty()) ? edges.get(0).getTargetNodeId() : null;

        if (nextNodeId != null) {
            log.debug("[PLAY] 播放语音后继续: nodeId={}, nextNode={}", currentNode.getNodeId(), nextNodeId);
            return IvrNodeHandlerResult.continueTo(nextNodeId, variables);
        }

        log.debug("[PLAY] 播放语音后无下一节点，流程结束: nodeId={}", currentNode.getNodeId());
        return IvrNodeHandlerResult.end(variables);
    }
}