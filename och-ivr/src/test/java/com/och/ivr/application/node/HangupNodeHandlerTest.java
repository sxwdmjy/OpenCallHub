package com.och.ivr.application.node;

import com.och.ivr.domain.IvrNode;
import com.och.ivr.domain.IvrNodeType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HangupNodeHandlerTest {

    private final HangupNodeHandler handler = new HangupNodeHandler();

    @Test
    void shouldReturnHangupActionWhenNodeIsValid() {
        IvrNode node = new IvrNode();
        node.setNodeId("hangup-node-1");
        node.setNodeType("HANGUP");
        node.setNodeName("挂断节点");

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("HANGUP");
        assertThat(result.getNextNodeId()).isNull();
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.HANGUP);
        assertThat(result.isTerminal()).isTrue();
    }

    @Test
    void shouldReturnHangupActionWithVariables() {
        IvrNode node = new IvrNode();
        node.setNodeId("hangup-node-2");
        node.setNodeType("HANGUP");

        Map<String, Object> variables = new HashMap<>();
        variables.put("callSid", "CA123456");
        variables.put("duration", 120);

        IvrNodeHandlerResult result = handler.handle(node, null, null, variables);

        assertThat(result.getAction()).isEqualTo("HANGUP");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.HANGUP);
        assertThat(result.getVariables()).isSameAs(variables);
        assertThat(result.getVariables()).containsEntry("callSid", "CA123456");
    }

    @Test
    void shouldReturnHangupActionWhenVariablesAreNull() {
        IvrNode node = new IvrNode();
        node.setNodeId("hangup-minimal");

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("HANGUP");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.HANGUP);
        assertThat(result.getVariables()).isNull();
        assertThat(result.isTerminal()).isTrue();
    }
}
