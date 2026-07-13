package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.system.domain.entity.CallSkill;
import com.och.system.domain.entity.CallSkillAgentRel;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;
import com.och.system.mapper.CallSkillMapper;
import com.och.system.service.ICallSkillAgentRelService;
import com.och.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CallSkillServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class CallSkillServiceTest {

    @Mock
    private CallSkillMapper callSkillMapper;

    @Mock
    private ICallSkillAgentRelService iCallSkillAgentRelService;

    @Mock
    private ISysUserService iSysUserService;

    @InjectMocks
    private CallSkillServiceImpl callSkillService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(callSkillService, "baseMapper", callSkillMapper);
    }

    // ── addSkill ──────────────────────────────────────────────────────────

    @Test
    void addSkill_shouldSaveSkillAndAssignAgents_whenAgentListProvided() {
        CallSkillAddQuery query = new CallSkillAddQuery();
        query.setGroupId(1L);
        query.setName("support");
        query.setStrategyType(1);
        query.setFullBusyType(0);
        query.setOverflowType(0);
        query.setTimeOut(30);
        query.setQueueLength(10);
        query.setAgentList(List.of(
                CallSkillAgentRel.builder().agentId(101L).build(),
                CallSkillAgentRel.builder().agentId(102L).build()
        ));

        doReturn(1).when(callSkillMapper).insert(any(CallSkill.class));

        callSkillService.add(query);

        verify(callSkillMapper).insert(any(CallSkill.class));
        verify(iCallSkillAgentRelService).addBySkillId(any(), anyList());
    }

    @Test
    void addSkill_shouldStillAssignAgents_whenAgentListEmpty() {
        CallSkillAddQuery query = new CallSkillAddQuery();
        query.setGroupId(1L);
        query.setName("support");
        query.setStrategyType(1);
        query.setFullBusyType(0);
        query.setOverflowType(0);
        query.setTimeOut(30);
        query.setQueueLength(10);
        query.setAgentList(List.of());

        doReturn(1).when(callSkillMapper).insert(any(CallSkill.class));

        callSkillService.add(query);

        verify(callSkillMapper).insert(any(CallSkill.class));
        verify(iCallSkillAgentRelService).addBySkillId(any(), anyList());
    }

    // ── getSkill ──────────────────────────────────────────────────────────

    @Test
    void getDetail_shouldReturnSkillVo_whenSkillExists() {
        Long id = 1L;
        CallSkillVo vo = new CallSkillVo();
        vo.setId(id);
        vo.setName("support");
        vo.setGroupId(1L);
        vo.setStrategyType(1);

        when(callSkillMapper.getDetail(id)).thenReturn(vo);

        CallSkillVo result = callSkillService.getDetail(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("support");
        verify(callSkillMapper).getDetail(id);
    }

    // ── listSkills ────────────────────────────────────────────────────────

    @Test
    void getList_shouldReturnSkillList() {
        CallSkillQuery query = new CallSkillQuery();
        CallSkillVo vo1 = new CallSkillVo();
        vo1.setId(1L);
        vo1.setName("support");
        CallSkillVo vo2 = new CallSkillVo();
        vo2.setId(2L);
        vo2.setName("sales");

        when(callSkillMapper.getList(query)).thenReturn(List.of(vo1, vo2));

        List<CallSkillVo> result = callSkillService.getList(query);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CallSkillVo::getName).containsExactly("support", "sales");
        verify(callSkillMapper).getList(query);
    }

    // ── assignAgents (edit) ──────────────────────────────────────────────

    @Test
    void edit_shouldUpdateSkillAndAgents() {
        CallSkillAddQuery query = new CallSkillAddQuery();
        query.setId(1L);
        query.setGroupId(1L);
        query.setName("support-updated");
        query.setStrategyType(2);
        query.setFullBusyType(0);
        query.setOverflowType(0);
        query.setTimeOut(60);
        query.setQueueLength(20);
        query.setAgentList(List.of(
                CallSkillAgentRel.builder().agentId(103L).build()
        ));

        doReturn(1).when(callSkillMapper).updateById(any(CallSkill.class));

        callSkillService.edit(query);

        verify(callSkillMapper).updateById(any(CallSkill.class));
        verify(iCallSkillAgentRelService).updateBySkillId(eq(1L), anyList());
    }

    // ── getAgentListBySkillId ────────────────────────────────────────────

    @Test
    void getAgentListBySkillId_shouldReturnAgentIds() {
        Long skillId = 1L;
        List<CallSkillAgentRel> agentRels = List.of(
                CallSkillAgentRel.builder().id(1L).skillId(skillId).agentId(101L).build(),
                CallSkillAgentRel.builder().id(2L).skillId(skillId).agentId(102L).build()
        );

        doReturn(agentRels).when(iCallSkillAgentRelService).list(any(LambdaQueryWrapper.class));

        List<Long> result = callSkillService.getAgentListBySkillId(skillId);

        assertThat(result).containsExactly(101L, 102L);
        verify(iCallSkillAgentRelService).list(any(LambdaQueryWrapper.class));
    }
}
