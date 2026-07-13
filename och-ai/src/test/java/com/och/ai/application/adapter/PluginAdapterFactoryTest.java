package com.och.ai.application.adapter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PluginAdapterFactoryTest {

    @Test
    void shouldSelectAdapterByType() {
        InternalBotAdapter internalBotAdapter = new InternalBotAdapter();
        PluginAdapterFactory factory = new PluginAdapterFactory(List.of(internalBotAdapter));

        BotAdapter adapter = factory.getAdapter("INTERNAL");

        assertThat(adapter).isSameAs(internalBotAdapter);
    }

    @Test
    void shouldNormalizeAdapterType() {
        InternalBotAdapter internalBotAdapter = new InternalBotAdapter();
        PluginAdapterFactory factory = new PluginAdapterFactory(List.of(internalBotAdapter));

        assertThat(factory.getAdapter(" internal ")).isSameAs(internalBotAdapter);
        assertThat(factory.getAdapter(null)).isSameAs(internalBotAdapter);
    }

    @Test
    void shouldRejectUnsupportedType() {
        PluginAdapterFactory factory = new PluginAdapterFactory(List.of(new InternalBotAdapter()));

        assertThatThrownBy(() -> factory.getAdapter("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的机器人引擎类型");
    }
}
