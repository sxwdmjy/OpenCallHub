package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CallSkillAgentRel;
import com.och.system.mapper.CallSkillAgentRelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CallSkillAgentRelServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class CallSkillAgentRelServiceTest {

    @Mock
    private CallSkillAgentRelMapper callSkillAgentRelMapper;

    @Spy
    @InjectMocks
    private CallSkillAgentRelServiceImpl callSkillAgentRelService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(callSkillAgentRelService, "baseMapper", callSkillAgentRelMapper);
    }

    // ── addBySkillId ──────────────────────────────────────────────────────

    @Test
    void addBySkillId_shouldSaveAgentRels_whenAgentListNotEmpty() {
        Long skillId = 1L;
        List<CallSkillAgentRel> agentList = List.of(
                CallSkillAgentRel.builder().agentId(101L).build(),
                CallSkillAgentRel.builder().agentId(102L).build()
        );

        doReturn(true).when(callSkillAgentRelService).saveBatch(anyList());

        callSkillAgentRelService.addBySkillId(skillId, agentList);

        verify(callSkillAgentRelService).saveBatch(anyList());
    }

    @Test
    void addBySkillId_shouldDoNothing_whenAgentListEmpty() {
        callSkillAgentRelService.addBySkillId(1L, Collections.emptyList());

        verify(callSkillAgentRelService, never()).saveBatch(anyList());
    }

    // ── deleteBySkillId ──────────────────────────────────────────────────

    @Test
    void deleteBySkillId_shouldSoftDeleteBySkillIds() {
        List<Long> skillIds = List.of(1L, 2L);

        doReturn(1).when(callSkillAgentRelMapper).update(any(CallSkillAgentRel.class), any(LambdaQueryWrapper.class));

        callSkillAgentRelService.deleteBySkillId(skillIds);

        verify(callSkillAgentRelMapper).update(any(CallSkillAgentRel.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void deleteBySkillId_shouldDoNothing_whenSkillIdsEmpty() {
        callSkillAgentRelService.deleteBySkillId(Collections.emptyList());

        verify(callSkillAgentRelMapper, never()).update(any(), any(LambdaQueryWrapper.class));
    }
}
