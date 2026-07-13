package com.och.esl.application;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EslEventNormalizerTest {

    private final EslEventNormalizer normalizer = new EslEventNormalizer();

    @Test
    void normalizeShouldPreferBusinessCallIdAndCurrentUuidAsLegId() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Event-Name", "CHANNEL_BRIDGE");
        headers.put("Event-Date-Timestamp", "1717214400123456");
        headers.put("Unique-ID", "11111111-2222-3333-4444-555555555555");
        headers.put("Other-Leg-Unique-ID", "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        headers.put("Caller-Unique-ID", "caller-uuid");
        headers.put("Caller-Caller-ID-Number", "13800138000");
        headers.put("Caller-Destination-Number", "8001");
        headers.put("variable_call_id", "biz-call-001");
        headers.put("variable_sip_call_id", "sip-call-001");
        headers.put("irrelevant-large-header", "ignored");

        NormalizedEslEvent event = normalizer.normalize(headers);

        assertThat(event.getEventType()).isEqualTo(EslEventType.CHANNEL_BRIDGE);
        assertThat(event.getCallId()).isEqualTo("biz-call-001");
        assertThat(event.getFreeswitchUuid()).isEqualTo("11111111-2222-3333-4444-555555555555");
        assertThat(event.getLegId()).isEqualTo("11111111-2222-3333-4444-555555555555");
        assertThat(event.getOtherLegId()).isEqualTo("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        assertThat(event.getSipCallId()).isEqualTo("sip-call-001");
        assertThat(event.getRawHeaders()).containsKeys("Event-Name", "Unique-ID", "variable_call_id", "variable_sip_call_id");
        assertThat(event.getRawHeaders()).doesNotContainKey("irrelevant-large-header");
        assertThat(event.getEventTime()).isEqualTo(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(1717214400123456L / 1000), ZoneId.systemDefault()));
    }
}
