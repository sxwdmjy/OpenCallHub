package com.och.acd.application.strategy;

import com.och.acd.domain.AcdAgentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 派发策略单元测试，覆盖 IdleFirst、LeastCalls、RoundRobin、SkillPriority 四种策略。
 *
 * @author danmo
 */
@DisplayName("派发策略测试")
class DispatchStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 15, 10, 0, 0);

    private static AcdAgentStatus buildAgent(Long agentId, Long skillGroupId, Integer skillLevel,
                                              Integer totalAnswered, long lastStateTimeOffsetSeconds) {
        return AcdAgentStatus.builder()
                .id(agentId)
                .agentId(agentId)
                .skillGroupId(skillGroupId)
                .skillLevel(skillLevel)
                .totalAnswered(totalAnswered)
                .lastStateTime(NOW.plusSeconds(lastStateTimeOffsetSeconds))
                .state("IDLE")
                .build();
    }

    // ========================================================================
    // IdleFirstStrategy
    // ========================================================================

    @Nested
    @DisplayName("IdleFirstStrategy - 空闲优先策略")
    class IdleFirstStrategyTest {

        private final IdleFirstStrategy strategy = new IdleFirstStrategy();

        @Test
        @DisplayName("非空列表返回第一个元素（空闲时间最长的坐席）")
        void shouldReturnFirstElementWhenListIsNotEmpty() {
            AcdAgentStatus agent1 = buildAgent(1L, 10L, 5, 0, -3600);
            AcdAgentStatus agent2 = buildAgent(2L, 10L, 5, 0, -1800);
            AcdAgentStatus agent3 = buildAgent(3L, 10L, 5, 0, -900);
            List<AcdAgentStatus> agents = List.of(agent1, agent2, agent3);

            AcdAgentStatus selected = strategy.select(agents);

            assertThat(selected).isNotNull();
            assertThat(selected.getAgentId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("空列表返回 null")
        void shouldReturnNullWhenListIsEmpty() {
            AcdAgentStatus selected = strategy.select(Collections.emptyList());
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("null 列表返回 null")
        void shouldReturnNullWhenListIsNull() {
            AcdAgentStatus selected = strategy.select(null);
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("getName 返回 IDLE_FIRST")
        void shouldReturnCorrectStrategyName() {
            assertThat(strategy.getName()).isEqualTo("IDLE_FIRST");
        }
    }

    // ========================================================================
    // LeastCallsStrategy
    // ========================================================================

    @Nested
    @DisplayName("LeastCallsStrategy - 最少通话策略")
    class LeastCallsStrategyTest {

        private final LeastCallsStrategy strategy = new LeastCallsStrategy();

        @Test
        @DisplayName("返回 totalAnswered 最小的坐席")
        void shouldReturnAgentWithLeastCalls() {
            AcdAgentStatus agent1 = buildAgent(1L, 10L, 5, 10, -3600);
            AcdAgentStatus agent2 = buildAgent(2L, 10L, 5, 3, -1800);
            AcdAgentStatus agent3 = buildAgent(3L, 10L, 5, 7, -900);
            List<AcdAgentStatus> agents = List.of(agent1, agent2, agent3);

            AcdAgentStatus selected = strategy.select(agents);

            assertThat(selected).isNotNull();
            assertThat(selected.getId()).isEqualTo(2L);
            assertThat(selected.getTotalAnswered()).isEqualTo(3);
        }

        @Test
        @DisplayName("totalAnswered 为 null 时视为 0")
        void shouldTreatNullTotalAnsweredAsZero() {
            AcdAgentStatus agent1 = buildAgent(1L, 10L, 5, null, -3600);
            AcdAgentStatus agent2 = buildAgent(2L, 10L, 5, 5, -1800);
            List<AcdAgentStatus> agents = List.of(agent1, agent2);

            AcdAgentStatus selected = strategy.select(agents);

            assertThat(selected).isNotNull();
            assertThat(selected.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("空列表返回 null")
        void shouldReturnNullWhenListIsEmpty() {
            AcdAgentStatus selected = strategy.select(Collections.emptyList());
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("null 列表返回 null")
        void shouldReturnNullWhenListIsNull() {
            AcdAgentStatus selected = strategy.select(null);
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("getName 返回 LEAST_CALLS")
        void shouldReturnCorrectStrategyName() {
            assertThat(strategy.getName()).isEqualTo("LEAST_CALLS");
        }
    }

    // ========================================================================
    // RoundRobinStrategy
    // ========================================================================

    @Nested
    @DisplayName("RoundRobinStrategy - 轮询策略")
    class RoundRobinStrategyTest {

        private final RoundRobinStrategy strategy = new RoundRobinStrategy();

        /**
         * 重置轮询计数器，避免测试间互相影响。
         */
        @BeforeEach
        void resetRoundRobinIndex() throws Exception {
            Field field = RoundRobinStrategy.class.getDeclaredField("roundRobinIndex");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Long, ?> indexMap = (Map<Long, ?>) field.get(strategy);
            indexMap.clear();
        }

        @Test
        @DisplayName("同一技能组内按顺序轮流分配")
        void shouldRotateThroughAgentsInOrder() {
            AcdAgentStatus agent1 = buildAgent(1L, 10L, 5, 0, -3600);
            AcdAgentStatus agent2 = buildAgent(2L, 10L, 5, 0, -1800);
            AcdAgentStatus agent3 = buildAgent(3L, 10L, 5, 0, -900);
            List<AcdAgentStatus> agents = List.of(agent1, agent2, agent3);

            assertThat(strategy.select(agents).getId()).isEqualTo(1L);
            assertThat(strategy.select(agents).getId()).isEqualTo(2L);
            assertThat(strategy.select(agents).getId()).isEqualTo(3L);
            // 轮完一圈回到第一个
            assertThat(strategy.select(agents).getId()).isEqualTo(1L);
            assertThat(strategy.select(agents).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("不同技能组维护独立的轮询计数器")
        void shouldMaintainSeparateCountersPerSkillGroup() {
            AcdAgentStatus agentA1 = buildAgent(1L, 10L, 5, 0, -3600);
            AcdAgentStatus agentA2 = buildAgent(2L, 10L, 5, 0, -1800);
            List<AcdAgentStatus> agentsA = List.of(agentA1, agentA2);

            AcdAgentStatus agentB1 = buildAgent(3L, 20L, 5, 0, -3600);
            AcdAgentStatus agentB2 = buildAgent(4L, 20L, 5, 0, -1800);
            AcdAgentStatus agentB3 = buildAgent(5L, 20L, 5, 0, -900);
            List<AcdAgentStatus> agentsB = List.of(agentB1, agentB2, agentB3);

            // 技能组 10: 第1个
            assertThat(strategy.select(agentsA).getId()).isEqualTo(1L);
            // 技能组 20: 第1个（独立计数）
            assertThat(strategy.select(agentsB).getId()).isEqualTo(3L);
            // 技能组 10: 第2个
            assertThat(strategy.select(agentsA).getId()).isEqualTo(2L);
            // 技能组 20: 第2个
            assertThat(strategy.select(agentsB).getId()).isEqualTo(4L);
        }

        @Test
        @DisplayName("单个坐席时始终返回该坐席")
        void shouldAlwaysReturnSameAgentWhenSingleAgent() {
            AcdAgentStatus agent1 = buildAgent(1L, 10L, 5, 0, -3600);
            List<AcdAgentStatus> agents = List.of(agent1);

            assertThat(strategy.select(agents).getId()).isEqualTo(1L);
            assertThat(strategy.select(agents).getId()).isEqualTo(1L);
            assertThat(strategy.select(agents).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("空列表返回 null")
        void shouldReturnNullWhenListIsEmpty() {
            AcdAgentStatus selected = strategy.select(Collections.emptyList());
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("null 列表返回 null")
        void shouldReturnNullWhenListIsNull() {
            AcdAgentStatus selected = strategy.select(null);
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("getName 返回 ROUND_ROBIN")
        void shouldReturnCorrectStrategyName() {
            assertThat(strategy.getName()).isEqualTo("ROUND_ROBIN");
        }
    }

    // ========================================================================
    // SkillPriorityStrategy
    // ========================================================================

    @Nested
    @DisplayName("SkillPriorityStrategy - 技能优先级策略")
    class SkillPriorityStrategyTest {

        private final SkillPriorityStrategy strategy = new SkillPriorityStrategy();

        @Test
        @DisplayName("返回 skillLevel 最高的坐席")
        void shouldReturnAgentWithHighestSkill() {
            AcdAgentStatus agent1 = buildAgent(1L, 10L, 3, 0, -3600);
            AcdAgentStatus agent2 = buildAgent(2L, 10L, 5, 0, -1800);
            AcdAgentStatus agent3 = buildAgent(3L, 10L, 2, 0, -900);
            List<AcdAgentStatus> agents = List.of(agent1, agent2, agent3);

            AcdAgentStatus selected = strategy.select(agents);

            assertThat(selected).isNotNull();
            assertThat(selected.getId()).isEqualTo(2L);
            assertThat(selected.getSkillLevel()).isEqualTo(5);
        }

        @Test
        @DisplayName("skillLevel 为 null 时视为 0，仍可能被选中（当所有坐席 skillLevel 都为 null 时）")
        void shouldTreatNullSkillLevelAsZeroAndStillSelect() {
            AcdAgentStatus agent1 = buildAgent(1L, 10L, null, 0, -3600);
            AcdAgentStatus agent2 = buildAgent(2L, 10L, null, 0, -1800);
            List<AcdAgentStatus> agents = List.of(agent1, agent2);

            AcdAgentStatus selected = strategy.select(agents);

            assertThat(selected).isNotNull();
        }

        @Test
        @DisplayName("空列表返回 null")
        void shouldReturnNullWhenListIsEmpty() {
            AcdAgentStatus selected = strategy.select(Collections.emptyList());
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("null 列表返回 null")
        void shouldReturnNullWhenListIsNull() {
            AcdAgentStatus selected = strategy.select(null);
            assertThat(selected).isNull();
        }

        @Test
        @DisplayName("getName 返回 SKILL_PRIORITY")
        void shouldReturnCorrectStrategyName() {
            assertThat(strategy.getName()).isEqualTo("SKILL_PRIORITY");
        }
    }
}
