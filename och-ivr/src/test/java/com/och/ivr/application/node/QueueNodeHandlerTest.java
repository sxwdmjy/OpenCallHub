package com.och.ivr.application.node;

import com.och.ivr.domain.IvrNode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QueueNodeHandlerTest {

    private final QueueNodeHandler handler = new QueueNodeHandler();

    @Test
    void shouldReadSkillGroupIdFromQueueNodeConfig() {
        IvrNode node = new IvrNode();
        node.setNodeId("transfer-skill-support");
        node.setNodeType("QUEUE");
        node.setConfigJson("{\"skillGroupId\":10}");
        Map<String, Object> variables = new HashMap<>();

        IvrNodeHandlerResult result = handler.handle(node, null, null, variables);

        assertThat(result.getAction()).isEqualTo("ENQUEUE");
        assertThat(result.getVariables())
                .containsEntry(IvrNodeHandlerResult.VARIABLE_SKILL_GROUP_ID, 10L)
                .containsEntry(IvrNodeHandlerResult.VARIABLE_QUEUE_NODE_ID, "transfer-skill-support");
    }

    @Test
    void shouldKeepQueueNodeIdWhenSkillGroupConfigIsMissing() {
        IvrNode node = new IvrNode();
        node.setNodeId("transfer-skill-default");
        node.setNodeType("TRANSFER_SKILL_GROUP");

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("ENQUEUE");
        assertThat(result.getVariables())
                .doesNotContainKey(IvrNodeHandlerResult.VARIABLE_SKILL_GROUP_ID)
                .containsEntry(IvrNodeHandlerResult.VARIABLE_QUEUE_NODE_ID, "transfer-skill-default");
    }
}
