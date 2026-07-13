package com.och.esl.application;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class EslCommandResultPublisherTest {

    private final EslCommandResultPublisher publisher = new EslCommandResultPublisher();

    @Test
    void completeShouldFinishPendingCommandAndRemoveIt() {
        CompletableFuture<PhoneControlCommandService.CommandResult> future =
                publisher.registerPending("cmd-1", "uuid_answer uuid-1", 30, TimeUnit.SECONDS);

        publisher.complete("cmd-1", new PhoneControlCommandService.CommandResult(
                "cmd-1", "uuid_answer uuid-1", true, null));

        assertThat(future).isCompleted();
        assertThat(future.join().accepted()).isTrue();
        assertThat(publisher.pendingCount()).isZero();
    }

    @Test
    void cleanExpiredShouldCompleteExpiredCommands() {
        CompletableFuture<PhoneControlCommandService.CommandResult> future =
                publisher.registerPending("cmd-2", "uuid_kill uuid-2", 30, TimeUnit.SECONDS);

        int removed = publisher.cleanExpired(-1);

        assertThat(removed).isEqualTo(1);
        assertThat(future).isCompleted();
        assertThat(future.join().accepted()).isFalse();
        assertThat(future.join().error()).isEqualTo("EXPIRED");
        assertThat(publisher.pendingCount()).isZero();
    }
}
