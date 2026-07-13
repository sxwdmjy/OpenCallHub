package com.och.ivr.application.node;

import com.och.ivr.domain.IvrNode;
import com.och.ivr.domain.IvrNodeType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TransferNodeHandlerTest {

    private final TransferNodeHandler handler = new TransferNodeHandler();

    @Test
    void shouldReturnTransferActionForInternalExtension() {
        IvrNode node = new IvrNode();
        node.setNodeId("transfer-1001");
        node.setNodeType("TRANSFER");
        node.setConfigJson("1001");

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("TRANSFER");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.TRANSFER);
        assertThat(result.getVariables()).containsEntry("transferTarget", "1001");
    }

    @Test
    void shouldReturnTransferActionForExternalNumber() {
        IvrNode node = new IvrNode();
        node.setNodeId("transfer-external");
        node.setNodeType("TRANSFER");
        node.setConfigJson("8613800138000");

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("TRANSFER");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.TRANSFER);
        assertThat(result.getVariables()).containsEntry("transferTarget", "8613800138000");
    }

    @Test
    void shouldNotContainTransferTargetWhenConfigJsonIsNull() {
        IvrNode node = new IvrNode();
        node.setNodeId("transfer-no-target");
        node.setNodeType("TRANSFER");
        node.setConfigJson(null);

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("TRANSFER");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.TRANSFER);
        assertThat(result.getVariables()).doesNotContainKey("transferTarget");
    }

    @Test
    void shouldPreserveVariablesWhenTransferring() {
        IvrNode node = new IvrNode();
        node.setNodeId("transfer-with-vars");
        node.setNodeType("TRANSFER");
        node.setConfigJson("2001");

        Map<String, Object> variables = new HashMap<>();
        variables.put("originalCaller", "Alice");
        variables.put("callCount", 3);

        IvrNodeHandlerResult result = handler.handle(node, null, null, variables);

        assertThat(result.getAction()).isEqualTo("TRANSFER");
        assertThat(result.getVariables()).isSameAs(variables);
        assertThat(result.getVariables())
                .containsEntry("transferTarget", "2001")
                .containsEntry("originalCaller", "Alice")
                .containsEntry("callCount", 3);
    }
}
