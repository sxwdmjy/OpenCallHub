package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语音信箱节点处理器（策略模式）
 * <p>
 * 将通话转入语音信箱，提示用户留言。返回 VOICEMAIL 动作标记，
 * 调用方按此标记触发 FreeSWITCH 录音并保存留言文件。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class VoicemailNodeHandler implements IvrNodeHandler {

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        log.info("[VOICEMAIL] 转入语音信箱: nodeId={}, nodeName={}", currentNode.getNodeId(), currentNode.getNodeName());

        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("voicemailConfig", currentNode.getConfigJson());
        return IvrNodeHandlerResult.voicemail(variables);
    }
}