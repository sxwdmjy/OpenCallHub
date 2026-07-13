package com.och.acd.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.acd.domain.AcdQueueSession;
import com.och.acd.domain.SkillGroup;
import com.och.acd.mapper.AcdQueueSessionMapper;
import com.och.acd.mapper.SkillGroupMapper;
import com.och.acd.service.impl.AcdOverflowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ACD 溢流服务单元测试，覆盖队列满和超时溢流两种场景。
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ACD 溢流服务测试")
class AcdOverflowServiceTest {

    @Mock
    private AcdQueueSessionMapper queueSessionMapper;

    @Mock
    private SkillGroupMapper skillGroupMapper;

    @InjectMocks
    private AcdOverflowServiceImpl overflowService;

    @BeforeEach
    void setUp() { ReflectionTestUtils.setField(overflowService, "baseMapper", queueSessionMapper); }

    // ========================================================================
    // isQueueFull
    // ========================================================================

    @Nested
    @DisplayName("isQueueFull - 队列满检查")
    class IsQueueFullTest {

        @Test
        @DisplayName("技能组不存在时返回 false")
        void shouldReturnFalseWhenSkillGroupNotFound() {
            when(skillGroupMapper.selectById(1L)).thenReturn(null);

            boolean result = overflowService.isQueueFull(1L);

            assertThat(result).isFalse();
            verify(queueSessionMapper, never()).selectCount(any());
        }

        @Test
        @DisplayName("技能组 maxQueueSize 为 null 时返回 false")
        void shouldReturnFalseWhenMaxQueueSizeIsNull() {
            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(null)
                    .maxWaitSeconds(60)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);

            boolean result = overflowService.isQueueFull(1L);

            assertThat(result).isFalse();
            verify(queueSessionMapper, never()).selectCount(any());
        }

        @Test
        @DisplayName("当前等待数小于最大容量时返回 false")
        void shouldReturnFalseWhenCurrentCountLessThanMax() {
            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(10)
                    .maxWaitSeconds(60)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);
            when(queueSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            boolean result = overflowService.isQueueFull(1L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("当前等待数等于最大容量时返回 true")
        void shouldReturnTrueWhenCurrentCountEqualsMax() {
            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(10)
                    .maxWaitSeconds(60)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);
            when(queueSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

            boolean result = overflowService.isQueueFull(1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("当前等待数大于最大容量时返回 true")
        void shouldReturnTrueWhenCurrentCountExceedsMax() {
            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(10)
                    .maxWaitSeconds(60)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);
            when(queueSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(15L);

            boolean result = overflowService.isQueueFull(1L);

            assertThat(result).isTrue();
        }
    }

    // ========================================================================
    // processTimeoutOverflow
    // ========================================================================

    @Nested
    @DisplayName("processTimeoutOverflow - 超时溢流处理")
    class ProcessTimeoutOverflowTest {

        @Test
        @DisplayName("技能组不存在时返回 0")
        void shouldReturnZeroWhenSkillGroupNotFound() {
            when(skillGroupMapper.selectById(1L)).thenReturn(null);

            int count = overflowService.processTimeoutOverflow(1L);

            assertThat(count).isZero();
            verify(queueSessionMapper, never()).selectList(any());
        }

        @Test
        @DisplayName("技能组 maxWaitSeconds 为 null 时返回 0")
        void shouldReturnZeroWhenMaxWaitSecondsIsNull() {
            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(10)
                    .maxWaitSeconds(null)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);

            int count = overflowService.processTimeoutOverflow(1L);

            assertThat(count).isZero();
            verify(queueSessionMapper, never()).selectList(any());
        }

        @Test
        @DisplayName("无等待中会话时返回 0")
        void shouldReturnZeroWhenNoWaitingSessions() {
            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(10)
                    .maxWaitSeconds(30)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);
            when(queueSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            int count = overflowService.processTimeoutOverflow(1L);

            assertThat(count).isZero();
            verify(queueSessionMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("超出最大等待时间的会话被标记为 OVERFLOW")
        void shouldMarkTimeoutSessionAsOverflow() {
            LocalDateTime enterTime = LocalDateTime.now().minusSeconds(61);
            AcdQueueSession session = AcdQueueSession.builder()
                    .id(100L)
                    .skillGroupId(1L)
                    .callId("call-001")
                    .state("WAITING")
                    .enterTime(enterTime)
                    .build();

            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(10)
                    .maxWaitSeconds(60)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);
            when(queueSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(session));

            int count = overflowService.processTimeoutOverflow(1L);

            assertThat(count).isEqualTo(1);

            ArgumentCaptor<AcdQueueSession> captor = ArgumentCaptor.forClass(AcdQueueSession.class);
            verify(queueSessionMapper).updateById(captor.capture());
            AcdQueueSession updated = captor.getValue();
            assertThat(updated.getState()).isEqualTo("OVERFLOW");
            assertThat(updated.getEndCause()).isEqualTo("TIMEOUT");
            assertThat(updated.getEndTime()).isNotNull();
            assertThat(updated.getWaitSeconds()).isNotNull();
            assertThat(updated.getWaitSeconds()).isGreaterThan(60);
        }

        @Test
        @DisplayName("未超出最大等待时间的会话不被标记")
        void shouldNotMarkSessionWithinMaxWaitTime() {
            LocalDateTime enterTime = LocalDateTime.now().minusSeconds(10);
            AcdQueueSession session = AcdQueueSession.builder()
                    .id(100L)
                    .skillGroupId(1L)
                    .callId("call-001")
                    .state("WAITING")
                    .enterTime(enterTime)
                    .build();

            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L)
                    .groupName("test-group")
                    .maxQueueSize(10)
                    .maxWaitSeconds(60)
                    .build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);
            when(queueSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(session));

            int count = overflowService.processTimeoutOverflow(1L);

            assertThat(count).isZero();
            verify(queueSessionMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("混合场景：部分超时部分未超时，仅超时的被标记")
        void shouldOnlyMarkTimeoutSessionsInMixedScenario() {
            LocalDateTime timeoutEnter = LocalDateTime.now().minusSeconds(91);
            LocalDateTime inTimeEnter = LocalDateTime.now().minusSeconds(10);
            LocalDateTime alsoTimeoutEnter = LocalDateTime.now().minusSeconds(61);

            AcdQueueSession timeoutSession1 = AcdQueueSession.builder()
                    .id(1L).skillGroupId(1L).callId("call-001").state("WAITING")
                    .enterTime(timeoutEnter).build();
            AcdQueueSession inTimeSession = AcdQueueSession.builder()
                    .id(2L).skillGroupId(1L).callId("call-002").state("WAITING")
                    .enterTime(inTimeEnter).build();
            AcdQueueSession timeoutSession2 = AcdQueueSession.builder()
                    .id(3L).skillGroupId(1L).callId("call-003").state("WAITING")
                    .enterTime(alsoTimeoutEnter).build();

            SkillGroup skillGroup = SkillGroup.builder()
                    .id(1L).groupName("test-group").maxQueueSize(10).maxWaitSeconds(60).build();
            when(skillGroupMapper.selectById(1L)).thenReturn(skillGroup);
            when(queueSessionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(timeoutSession1, inTimeSession, timeoutSession2));

            int count = overflowService.processTimeoutOverflow(1L);

            assertThat(count).isEqualTo(2);
            // 仅两个超时会话被更新
            verify(queueSessionMapper, times(2)).updateById(any());
        }
    }
}
