package com.och.system.service.impl;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.system.domain.entity.SipAgent;
import com.och.system.domain.query.agent.SipAgentAddQuery;
import com.och.system.domain.vo.agent.SipAgentStatusVo;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.mapper.SipAgentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * SipAgentService 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SipAgentService 单元测试")
class SipAgentServiceTest {

    @Mock
    private SipAgentMapper sipAgentMapper;

    @Mock
    private RedisService redisService;

    private SipAgentServiceImpl sipAgentService;

    @BeforeEach
    void setUp() throws Exception {
        sipAgentService = new SipAgentServiceImpl(redisService);
        // Inject baseMapper into the parent ServiceImpl class
        Field baseMapperField = sipAgentService.getClass()
                .getSuperclass().getSuperclass()
                .getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(sipAgentService, sipAgentMapper);
    }

    @Nested
    @DisplayName("add - 新增坐席")
    class AddTests {

        @Test
        @DisplayName("should save agent successfully")
        void shouldSaveAgentSuccessfully() {
            // given
            SipAgentAddQuery query = new SipAgentAddQuery();
            query.setName("test-agent");
            query.setUserId(1L);
            query.setAgentNumber("1001");
            query.setStatus(1);

            when(sipAgentMapper.insert(any(SipAgent.class))).thenReturn(1);

            // when
            sipAgentService.add(query);

            // then
            ArgumentCaptor<SipAgent> captor = ArgumentCaptor.forClass(SipAgent.class);
            verify(sipAgentMapper).insert(captor.capture());
            SipAgent saved = captor.getValue();
            assertEquals("test-agent", saved.getName());
            assertEquals(Long.valueOf(1L), saved.getUserId());
            assertEquals("1001", saved.getAgentNumber());
            assertEquals(Integer.valueOf(1), saved.getStatus());
        }
    }

    @Nested
    @DisplayName("getDetail - 查询坐席详情")
    class GetDetailTests {

        @Test
        @DisplayName("should return agent detail by id")
        void shouldReturnAgentDetailById() {
            // given
            Long agentId = 1L;
            SipAgentVo vo = new SipAgentVo();
            vo.setId(agentId);
            vo.setName("test-agent");
            vo.setAgentNumber("1001");
            vo.setStatus(1);
            vo.setOnlineStatus(1);

            when(sipAgentMapper.getDetail(agentId)).thenReturn(vo);

            // when
            SipAgentVo result = sipAgentService.getDetail(agentId);

            // then
            assertNotNull(result);
            assertEquals(agentId, result.getId());
            assertEquals("test-agent", result.getName());
            assertEquals("1001", result.getAgentNumber());
            assertEquals(Integer.valueOf(1), result.getStatus());
            assertEquals(Integer.valueOf(1), result.getOnlineStatus());
        }
    }

    @Nested
    @DisplayName("updateStatus - 更新坐席状态")
    class UpdateStatusTests {

        @Test
        @DisplayName("should update agent status successfully")
        void shouldUpdateAgentStatusSuccessfully() {
            // given
            Long agentId = 1L;
            Integer newStatus = 1;

            when(sipAgentMapper.updateById(any(SipAgent.class))).thenReturn(1);

            // when
            Boolean result = sipAgentService.updateStatus(agentId, newStatus);

            // then
            assertTrue(result);
            verify(sipAgentMapper).updateById(argThat(agent ->
                    agent.getId().equals(agentId) && agent.getStatus().equals(newStatus)));
        }

        @Test
        @DisplayName("should return false when status update fails")
        void shouldReturnFalseWhenStatusUpdateFails() {
            // given
            Long agentId = 1L;
            Integer newStatus = 2;

            when(sipAgentMapper.updateById(any(SipAgent.class))).thenReturn(0);

            // when
            Boolean result = sipAgentService.updateStatus(agentId, newStatus);

            // then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getAgentStatusList - 获取坐席状态列表")
    class GetAgentStatusListTests {

        @Test
        @DisplayName("should return agent status list from redis when cache hit")
        void shouldReturnAgentStatusListFromRedisWhenCacheHit() {
            // given
            List<Long> agentIds = List.of(1L, 2L);

            SipAgentStatusVo statusVo1 = new SipAgentStatusVo();
            statusVo1.setId(1L);
            statusVo1.setName("agent-1");
            statusVo1.setOnlineStatus(1);

            SipAgentStatusVo statusVo2 = new SipAgentStatusVo();
            statusVo2.setId(2L);
            statusVo2.setName("agent-2");
            statusVo2.setOnlineStatus(5);

            Map<String, SipAgentStatusVo> cacheMap = new HashMap<>();
            cacheMap.put("1", statusVo1);
            cacheMap.put("2", statusVo2);

            doReturn(cacheMap).when(redisService).getCacheMap(CacheConstants.AGENT_CURRENT_STATUS_KEY);

            // when
            List<SipAgentStatusVo> result = sipAgentService.getAgentStatusList(agentIds);

            // then
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(s -> s.getId().equals(1L)));
            assertTrue(result.stream().anyMatch(s -> s.getId().equals(2L)));
        }

        @Test
        @DisplayName("should return empty list when redis cache is empty")
        void shouldReturnEmptyListWhenRedisCacheIsEmpty() {
            // given
            List<Long> agentIds = List.of(1L);

            doReturn(Collections.emptyMap()).when(redisService).getCacheMap(CacheConstants.AGENT_CURRENT_STATUS_KEY);

            // when
            List<SipAgentStatusVo> result = sipAgentService.getAgentStatusList(agentIds);

            // then
            assertTrue(result.isEmpty());
        }
    }
}
