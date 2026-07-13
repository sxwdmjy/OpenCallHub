package com.och.esl.application;

import com.och.esl.config.EslConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EslConnectionManagerTest {

    @Test
    void connectShouldMarkNodeConnectedAndRecordStats() {
        RecordingConnectionClient client = new RecordingConnectionClient(true);
        EslConnectionManager manager = newManager(client);

        manager.connect("fs-1", "127.0.0.1", 8021, "ClueCon");

        assertThat(manager.getConnectionState("fs-1")).isEqualTo(EslConnectionManager.ConnectionState.CONNECTED);
        assertThat(manager.isConnected("fs-1")).isTrue();
        assertThat(manager.getConnectionStats("fs-1").connectCount()).isEqualTo(1);
        assertThat(client.requests()).hasSize(1);
        assertThat(client.requests().get(0).timeoutMs()).isEqualTo(10000);
    }

    @Test
    void connectRejectedShouldMarkNodeFailed() {
        RecordingConnectionClient client = new RecordingConnectionClient(false);
        EslConnectionManager manager = newManager(client);

        manager.connect("fs-2", "127.0.0.1", 8021, "ClueCon");

        assertThat(manager.getConnectionState("fs-2")).isEqualTo(EslConnectionManager.ConnectionState.FAILED);
        assertThat(manager.isConnected("fs-2")).isFalse();
    }

    @Test
    void disconnectShouldDisableConnectionAndUpdateStats() {
        RecordingConnectionClient client = new RecordingConnectionClient(true);
        EslConnectionManager manager = newManager(client);
        manager.connect("fs-3", "127.0.0.1", 8021, "ClueCon");

        manager.disconnect("fs-3");

        assertThat(manager.getConnectionState("fs-3")).isEqualTo(EslConnectionManager.ConnectionState.DISCONNECTED);
        assertThat(manager.getConnectionStats("fs-3").disconnectCount()).isEqualTo(1);
        assertThat(client.disconnected()).containsExactly("fs-3");
    }

    @Test
    void reconnectShouldReuseCachedConnectionConfig() {
        RecordingConnectionClient client = new RecordingConnectionClient(true);
        EslConnectionManager manager = newManager(client);
        manager.connect("fs-4", "10.0.0.4", 8021, "ClueCon");

        manager.reconnect("fs-4");

        assertThat(client.requests()).hasSize(2);
        assertThat(client.requests().get(1).freeswitchId()).isEqualTo("fs-4");
        assertThat(client.requests().get(1).host()).isEqualTo("10.0.0.4");
        assertThat(manager.getConnectionStats("fs-4").connectCount()).isEqualTo(2);
    }

    private EslConnectionManager newManager(EslConnectionClient client) {
        return new EslConnectionManager(client, new EslConfigurationProperties(), mock(ObjectProvider.class));
    }

    private static class RecordingConnectionClient implements EslConnectionClient {
        private final boolean connected;
        private final List<ConnectionRequest> requests = new ArrayList<>();
        private final List<String> disconnected = new ArrayList<>();

        private RecordingConnectionClient(boolean connected) {
            this.connected = connected;
        }

        @Override
        public ConnectionResult connect(ConnectionRequest request, Consumer<Map<String, String>> eventConsumer) {
            requests.add(request);
            return new ConnectionResult(connected, connected ? null : "CONNECT_FAILED");
        }

        @Override
        public void disconnect(String freeswitchId) {
            disconnected.add(freeswitchId);
        }

        private List<ConnectionRequest> requests() {
            return requests;
        }

        private List<String> disconnected() {
            return disconnected;
        }
    }
}
