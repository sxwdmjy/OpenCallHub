package com.och.ivr.application.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 排队节点处理器（策略模式）
 * <p>
 * 将通话转入 ACD 排队队列，返回 ENQUEUE 动作标记。
 * 调用方根据结果变量中的 skillGroupId 触发具体技能组分配，避免把 IVR 流程 ID 误用为 ACD 技能组 ID。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class QueueNodeHandler implements IvrNodeHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        Long skillGroupId = parseSkillGroupId(currentNode);
        log.info("[QUEUE] 转入排队: nodeId={}, nodeName={}, skillGroupId={}",
                currentNode.getNodeId(), currentNode.getNodeName(), skillGroupId);
        return IvrNodeHandlerResult.enqueue(skillGroupId, currentNode.getNodeId(), variables);
    }

    private Long parseSkillGroupId(IvrNode currentNode) {
        String configJson = currentNode.getConfigJson();
        if (configJson == null || configJson.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> config = OBJECT_MAPPER.readValue(configJson, MAP_TYPE);
            Object value = config.get(IvrNodeHandlerResult.VARIABLE_SKILL_GROUP_ID);
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String text && !text.isBlank()) {
                return Long.valueOf(text);
            }
        } catch (Exception e) {
            log.warn("[QUEUE] 队列节点配置解析失败: nodeId={}, config={}", currentNode.getNodeId(), configJson, e);
        }
        return null;
    }
}