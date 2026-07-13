package com.och.calltask.application.algorithm;

import com.och.calltask.domain.DialResult;
import com.och.calltask.domain.OutboundDialAttempt;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

/**
 * Unit tests for {@link SimpleRollingAverageAlgorithm}.
 * <p>
 * Covers predictAnswerProbability, calculateOptimalDialCount, updateModel,
 * sliding window overflow, and thread safety.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SimpleRollingAverageAlgorithmTest {

    @Mock
    private OutboundDialAttemptMapper dialAttemptMapper;

    private SimpleRollingAverageAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new SimpleRollingAverageAlgorithm(dialAttemptMapper);
        // Do NOT call init() — start with an empty window for controlled test setup
    }

    // ======================== predictAnswerProbability ========================

    @Test
    void predictAnswerProbability_shouldReturnDefaultRateForEmptyWindow() {
        double rate = algorithm.predictAnswerProbability("13800138000");

        assertThat(rate).isCloseTo(0.3, byLessThan(0.001));
    }

    @Test
    void predictAnswerProbability_shouldReturnCalculatedRateAfterUpdates() {
        // Add 10 CONNECTED + 10 FAILED = 20 total, 10 connected => rate = 0.5
        addResults(algorithm, 10, DialResult.CONNECTED);
        addResults(algorithm, 10, DialResult.FAILED);

        double rate = algorithm.predictAnswerProbability("13800138000");

        assertThat(rate).isCloseTo(0.5, byLessThan(0.001));
    }

    @Test
    void predictAnswerProbability_shouldReturnOneForAllConnected() {
        addResults(algorithm, 5, DialResult.CONNECTED);

        double rate = algorithm.predictAnswerProbability("13800138000");

        assertThat(rate).isCloseTo(1.0, byLessThan(0.001));
    }

    @Test
    void predictAnswerProbability_shouldReturnZeroForNoConnection() {
        addResults(algorithm, 10, DialResult.NO_ANSWER);

        double rate = algorithm.predictAnswerProbability("13800138000");

        assertThat(rate).isCloseTo(0.0, byLessThan(0.001));
    }

    // ======================== calculateOptimalDialCount ========================

    @Test
    void calculateOptimalDialCount_shouldReturnZeroWhenNoIdleAgents() {
        int count = algorithm.calculateOptimalDialCount(0, 0.5);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void calculateOptimalDialCount_shouldReturnAtLeastOne() {
        int count = algorithm.calculateOptimalDialCount(1, 0.0);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void calculateOptimalDialCount_shouldUseMultiplierFormula() {
        // idleAgents=5, answerRate=0.5 -> 5 + round(5 * 0.5 * 1.2) = 5 + round(3.0) = 8
        int count = algorithm.calculateOptimalDialCount(5, 0.5);

        assertThat(count).isEqualTo(8);
    }

    // ======================== updateModel ========================

    @Test
    void updateModel_shouldDoNothingForNullAttempt() {
        double rateBefore = algorithm.predictAnswerProbability("13800138000");

        algorithm.updateModel(null);

        double rateAfter = algorithm.predictAnswerProbability("13800138000");
        assertThat(rateAfter).isEqualTo(rateBefore);
    }

    @Test
    void updateModel_shouldDoNothingForNullDialResult() {
        OutboundDialAttempt attempt = OutboundDialAttempt.builder()
                .id(1L)
                .dialResult(null)
                .dialTime(LocalDateTime.now())
                .build();

        double rateBefore = algorithm.predictAnswerProbability("13800138000");

        algorithm.updateModel(attempt);

        double rateAfter = algorithm.predictAnswerProbability("13800138000");
        assertThat(rateAfter).isEqualTo(rateBefore);
    }

    // ======================== sliding window overflow ========================

    @Test
    void slidingWindow_shouldDropOldestWhenExceedsCapacity() {
        // Window max is 100. Fill beyond while tracking rate changes.
        // First, fill with 100 CONNECTED => rate should be 1.0
        addResults(algorithm, 100, DialResult.CONNECTED);

        double rateAfterConnected = algorithm.predictAnswerProbability("any");
        assertThat(rateAfterConnected).isCloseTo(1.0, byLessThan(0.001));

        // Then add 50 FAILED. The oldest 50 CONNECTED should be dropped,
        // leaving 50 CONNECTED + 50 FAILED => rate = 0.5
        addResults(algorithm, 50, DialResult.FAILED);

        double rateAfterMixed = algorithm.predictAnswerProbability("any");
        assertThat(rateAfterMixed).isCloseTo(0.5, byLessThan(0.001));

        // Add 50 more FAILED. Now all 100 items should be FAILED => rate = 0.0
        addResults(algorithm, 50, DialResult.FAILED);

        double rateAfterAllFailed = algorithm.predictAnswerProbability("any");
        assertThat(rateAfterAllFailed).isCloseTo(0.0, byLessThan(0.001));
    }

    @Test
    void slidingWindow_shouldMaintainCorrectCountAfterOverflow() {
        // Add 150 total items (100 CONNECTED, then 50 FAILED)
        addResults(algorithm, 100, DialResult.CONNECTED);
        addResults(algorithm, 50, DialResult.FAILED);

        // Window size should be capped at 100 (50 CONNECTED + 50 FAILED)
        double rate = algorithm.predictAnswerProbability("any");
        assertThat(rate).isCloseTo(0.5, byLessThan(0.001));
    }

    // ======================== thread safety ========================

    @Test
    void updateModel_shouldBeThreadSafe() throws InterruptedException {
        int threadCount = 4;
        int itemsPerThread = 30; // Total: 120 > 100 window
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < itemsPerThread; i++) {
                        // Alternate CONNECTED and FAILED within each thread
                        DialResult result = (i % 2 == 0) ? DialResult.CONNECTED : DialResult.FAILED;
                        OutboundDialAttempt attempt = OutboundDialAttempt.builder()
                                .id((long) (threadId * itemsPerThread + i))
                                .dialResult(result.name())
                                .dialTime(LocalDateTime.now())
                                .build();
                        algorithm.updateModel(attempt);
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(errors.get()).isEqualTo(0);

        // Each thread contributes 15 CONNECTED + 15 FAILED.
        // Window caps at 100, with balanced ratio of last 100 entries => ~0.5
        double rate = algorithm.predictAnswerProbability("any");
        assertThat(rate).isCloseTo(0.5, byLessThan(0.05));
    }

    // ======================== helper ========================

    private void addResults(SimpleRollingAverageAlgorithm algorithm, int count, DialResult result) {
        for (int i = 0; i < count; i++) {
            OutboundDialAttempt attempt = OutboundDialAttempt.builder()
                    .id((long) i)
                    .dialResult(result.name())
                    .dialTime(LocalDateTime.now().minusMinutes(count - i))
                    .build();
            algorithm.updateModel(attempt);
        }
    }
}
