package com.och.esl.application;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneControlCommandServiceTest {

    private final RecordingCommandClient commandClient = new RecordingCommandClient();
    private final PhoneControlCommandService service = new PhoneControlCommandService(commandClient);

    @Test
    void answerShouldSendUuidAnswerCommand() {
        PhoneControlCommandService.CommandResult result =
                service.answer("11111111-2222-3333-4444-555555555555");

        assertThat(result.accepted()).isTrue();
        assertThat(result.command()).isEqualTo("uuid_answer 11111111-2222-3333-4444-555555555555");
        assertThat(commandClient.commands()).containsExactly(result.command());
    }

    @Test
    void playbackShouldAllowSafeRelativeMediaPath() {
        PhoneControlCommandService.CommandResult result =
                service.playback("11111111-2222-3333-4444-555555555555", "ivr/welcome.wav");

        assertThat(result.command()).isEqualTo("uuid_playback 11111111-2222-3333-4444-555555555555 ivr/welcome.wav");
    }

    @Test
    void originateShouldRejectUnsafeChannelVar() {
        assertThatThrownBy(() -> service.originate("1001", "8000", Map.of("caller", "1;shutdown")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("通道变量值包含非法字符");
    }

    private static class RecordingCommandClient implements EslCommandClient {
        private final List<String> commands = new ArrayList<>();

        @Override
        public PhoneControlCommandService.CommandResult sendCommand(String commandType, String commandId, String command) {
            commands.add(command);
            return new PhoneControlCommandService.CommandResult(commandId, command, true, null);
        }

        private List<String> commands() {
            return commands;
        }
    }
}
