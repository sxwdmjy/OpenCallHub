package com.och.ivr.application.node;

import com.och.ivr.domain.IvrNode;
import com.och.ivr.domain.IvrNodeType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VoicemailNodeHandlerTest {

    private final VoicemailNodeHandler handler = new VoicemailNodeHandler();

    @Test
    void shouldReturnVoicemailActionWithMaxDurationConfig() {
        IvrNode node = new IvrNode();
        node.setNodeId("voicemail-support");
        node.setNodeType("TRANSFER_VOICEMAIL");
        node.setNodeName("客服语音信箱");
        node.setConfigJson("{\"maxDuration\":60,\"beepBeforeRecord\":true}");

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("VOICEMAIL");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.TRANSFER_VOICEMAIL);
        assertThat(result.getVariables())
                .containsEntry("voicemailConfig", "{\"maxDuration\":60,\"beepBeforeRecord\":true}");
    }

    @Test
    void shouldHandleNullConfigJson() {
        IvrNode node = new IvrNode();
        node.setNodeId("voicemail-no-config");
        node.setNodeType("TRANSFER_VOICEMAIL");
        node.setConfigJson(null);

        IvrNodeHandlerResult result = handler.handle(node, null, null, null);

        assertThat(result.getAction()).isEqualTo("VOICEMAIL");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.TRANSFER_VOICEMAIL);
        assertThat(result.getVariables()).containsEntry("voicemailConfig", null);
    }

    @Test
    void shouldPreserveExistingVariables() {
        IvrNode node = new IvrNode();
        node.setNodeId("voicemail-with-vars");
        node.setNodeType("TRANSFER_VOICEMAIL");
        node.setConfigJson("{\"maxDuration\":120}");

        Map<String, Object> variables = new HashMap<>();
        variables.put("callerNumber", "13800138000");
        variables.put("attemptCount", 2);

        IvrNodeHandlerResult result = handler.handle(node, null, null, variables);

        assertThat(result.getAction()).isEqualTo("VOICEMAIL");
        assertThat(result.getVariables()).isSameAs(variables);
        assertThat(result.getVariables())
                .containsEntry("voicemailConfig", "{\"maxDuration\":120}")
                .containsEntry("callerNumber", "13800138000")
                .containsEntry("attemptCount", 2);
    }

    @Test
    void shouldSetVoicemailConfigWithShortDuration() {
        IvrNode node = new IvrNode();
        node.setNodeId("voicemail-short");
        node.setNodeType("TRANSFER_VOICEMAIL");
        node.setNodeName("短留言");
        node.setConfigJson("{\"maxDuration\":10}");

        IvrNodeHandlerResult result = handler.handle(node, null, null, new HashMap<>());

        assertThat(result.getAction()).isEqualTo("VOICEMAIL");
        assertThat(result.getNextNodeType()).isEqualTo(IvrNodeType.TRANSFER_VOICEMAIL);
        assertThat(result.getVariables())
                .containsEntry("voicemailConfig", "{\"maxDuration\":10}");
    }
}
