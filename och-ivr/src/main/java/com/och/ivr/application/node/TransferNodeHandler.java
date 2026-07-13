package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 转接节点处理器（策略模式）
 * <p>
 * 从节点配置JSON中提取转接目标号码，返回 TRANSFER 动作标记。
 * 调用方按目标号码执行盲转或咨询转。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class TransferNodeHandler implements IvrNodeHandler {

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        String target = currentNode.getConfigJson();
        log.info("[TRANSFER] 转接: nodeId={}, target={}", currentNode.getNodeId(), target);
        return IvrNodeHandlerResult.transfer(target, variables);
    }
}