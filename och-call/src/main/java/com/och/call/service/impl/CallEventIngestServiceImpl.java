package com.och.call.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.och.call.domain.CallEvent;
import com.och.call.application.CallStateMachine;
import com.och.call.domain.CallSession;
import com.och.call.application.CallStateMachine;
import com.och.call.domain.CallState;
import com.och.call.application.CallStateMachine;
import com.och.call.mapper.CallEventMapper;
import com.och.call.mapper.CallSessionMapper;
import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 呼叫事件摄取服务。
 * <p>
 * 统一接收 `och-esl` 发布的标准化通话事件，落库到 `call_event`，再按状态机更新 `call_session`。
 * 这里是 ESL 通信事件进入呼叫核心的门面入口，负责幂等、乱序保护和通话状态流转，
 * 不承载 IVR、ACD、外呼等后续业务策略。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallEventIngestServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.call.mapper.CallSessionMapper, com.och.call.domain.CallSession> implements com.och.call.service.ICallEventIngestService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CallEventMapper eventMapper;
    private final CallStateMachine stateMachine;

    /**
     * 监听 `och-esl` 发布的标准化 ESL 事件。
     */
    @EventListener
    @Transactional
    public void onNormalizedEslEvent(NormalizedEslEvent eslEvent) {
        ingest(eslEvent);
    }

    /**
     * 摄入标准化 ESL 事件。
     * <p>该入口保留 ESL 事件中的号码、原始头摘要等信息，用于首次创建通话会话。</p>
     */
    @Transactional
    public void ingest(NormalizedEslEvent eslEvent) {
        if (eslEvent == null || isBlank(eslEvent.getCallId())) {
            log.debug("[CallEventIngest] 忽略缺少 callId 的 ESL 事件: {}", eslEvent);
            return;
        }
        ingestInternal(toCallEvent(eslEvent), eslEvent);
    }

    /**
     * 摄入调试或测试直接提交的呼叫事件。
     */
    @Transactional
    public void ingest(CallEvent event) {
        if (event == null || isBlank(event.getCallId()) || isBlank(event.getEventType())) {
            log.debug("[CallEventIngest] 忽略缺少关键字段的呼叫事件: {}", event);
            return;
        }
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        if (isBlank(event.getDedupKey())) {
            event.setDedupKey(buildDedupKey(event));
        }
        ingestInternal(event, null);
    }

    private void ingestInternal(CallEvent event, NormalizedEslEvent eslEvent) {
        CallEvent storedEvent = findEventByDedupKey(event.getDedupKey());
        if (storedEvent != null) {
            if (Boolean.TRUE.equals(storedEvent.getProcessed())) {
                log.debug("[CallEventIngest] 重复事件已忽略: dedupKey={}", event.getDedupKey());
                return;
            }
            processStoredEvent(storedEvent, eslEvent);
            return;
        }

        event.setProcessed(false);
        try {
            eventMapper.insert(event);
        } catch (DuplicateKeyException e) {
            CallEvent retryEvent = findEventByDedupKey(event.getDedupKey());
            if (retryEvent != null && !Boolean.TRUE.equals(retryEvent.getProcessed())) {
                processStoredEvent(retryEvent, eslEvent);
            }
            return;
        }

        processStoredEvent(event, eslEvent);
    }

    private void processStoredEvent(CallEvent event, NormalizedEslEvent eslEvent) {
        try {
            processEvent(event, eslEvent);
            event.setProcessed(true);
            eventMapper.updateById(event);
        } catch (Exception e) {
            log.error("[CallEventIngest] 事件处理失败: callId={}, eventType={}, dedupKey={}",
                    event.getCallId(), event.getEventType(), event.getDedupKey(), e);
        }
    }

    private CallEvent toCallEvent(NormalizedEslEvent eslEvent) {
        EslEventType type = eslEvent.getEventTypeEnum();
        LocalDateTime eventTime = eslEvent.getEventTime() == null ? LocalDateTime.now() : eslEvent.getEventTime();
        String eventType = type.name();
        return CallEvent.builder()
                .callId(eslEvent.getCallId())
                .eventType(eventType)
                .eventTime(eventTime)
                .freeswitchUuid(eslEvent.getFreeswitchUuid())
                .legId(eslEvent.getLegId())
                .rawHeaders(toRawHeaderJson(eslEvent.getRawHeaders()))
                .dedupKey(buildDedupKey(eslEvent.getCallId(), eventType, eslEvent.getFreeswitchUuid(), eventTime))
                .build();
    }

    private void processEvent(CallEvent event, NormalizedEslEvent eslEvent) {
        CallSession session = findSessionByCallId(event.getCallId());
        if (session == null) {
            baseMapper.insert(createSessionFromEvent(event, eslEvent));
            return;
        }

        CallState currentState = parseState(session.getState());
        CallState newState = mapEventToState(event.getEventType());
        if (newState != null && currentState != newState) {
            if (currentState.isTerminal() && !newState.isTerminal()) {
                log.debug("[CallEventIngest] 终态会话忽略乱序事件: callId={}, current={}, eventType={}",
                        event.getCallId(), currentState, event.getEventType());
                return;
            }
            if (stateMachine.isValidTransition(currentState, newState)) {
                session.setState(newState.name());
            } else {
                log.warn("[CallEventIngest] 非法状态转换已忽略: callId={}, {} -> {}, eventType={}",
                        event.getCallId(), currentState, newState, event.getEventType());
            }
        }

        updateSessionFromEvent(session, event, eslEvent);
        baseMapper.updateById(session);
    }

    private CallSession createSessionFromEvent(CallEvent event, NormalizedEslEvent eslEvent) {
        CallState initialState = mapEventToState(event.getEventType());
        if (initialState == null) {
            initialState = CallState.NEW;
        }
        return CallSession.builder()
                .callId(event.getCallId())
                .callerNumber(eslEvent == null ? null : eslEvent.getCallerNumber())
                .calledNumber(eslEvent == null ? null : eslEvent.getCalledNumber())
                .direction(inferDirection(eslEvent))
                .state(initialState.name())
                .freeswitchUuid(event.getFreeswitchUuid())
                .startTime(event.getEventTime())
                .answerTime(initialState == CallState.BRIDGED ? event.getEventTime() : null)
                .endTime(initialState.isTerminal() ? event.getEventTime() : null)
                .endCause(eslEvent == null ? null : eslEvent.getHangupCause())
                .build();
    }

    private void updateSessionFromEvent(CallSession session, CallEvent event, NormalizedEslEvent eslEvent) {
        if (!isBlank(event.getFreeswitchUuid())) {
            session.setFreeswitchUuid(event.getFreeswitchUuid());
        }
        if (session.getStartTime() == null) {
            session.setStartTime(event.getEventTime());
        }
        if (eslEvent != null) {
            if (isBlank(session.getCallerNumber())) {
                session.setCallerNumber(eslEvent.getCallerNumber());
            }
            if (isBlank(session.getCalledNumber())) {
                session.setCalledNumber(eslEvent.getCalledNumber());
            }
            if (isBlank(session.getDirection())) {
                session.setDirection(inferDirection(eslEvent));
            }
        }

        switch (event.getEventType()) {
            case "CHANNEL_ANSWER", "CHANNEL_BRIDGE" -> {
                if (session.getAnswerTime() == null) {
                    session.setAnswerTime(event.getEventTime());
                }
            }
            case "CHANNEL_HANGUP", "CHANNEL_DESTROY" -> {
                session.setEndTime(event.getEventTime());
                if (eslEvent != null) {
                    session.setEndCause(eslEvent.getHangupCause());
                }
                if (session.getStartTime() != null && session.getEndTime() != null) {
                    session.setDurationSeconds((int) Math.max(0,
                            Duration.between(session.getStartTime(), session.getEndTime()).getSeconds()));
                }
            }
            default -> {
                // 其他事件只影响状态，不直接改写时间节点。
            }
        }
    }

    private CallEvent findEventByDedupKey(String dedupKey) {
        return eventMapper.selectOne(new LambdaQueryWrapper<CallEvent>().eq(CallEvent::getDedupKey, dedupKey));
    }

    private CallSession findSessionByCallId(String callId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<CallSession>().eq(CallSession::getCallId, callId));
    }

    private CallState parseState(String state) {
        if (isBlank(state)) {
            return CallState.NEW;
        }
        try {
            return CallState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.warn("[CallEventIngest] 未知通话状态，按 NEW 处理: state={}", state);
            return CallState.NEW;
        }
    }

    private CallState mapEventToState(String eventType) {
        return switch (eventType) {
            case "CHANNEL_CREATE", "CHANNEL_OUTGOING", "CHANNEL_PROGRESS", "CHANNEL_PROGRESS_MEDIA" -> CallState.RINGING;
            case "CHANNEL_ANSWER", "CHANNEL_BRIDGE" -> CallState.BRIDGED;
            case "CHANNEL_HOLD" -> CallState.HOLD;
            case "CHANNEL_UNHOLD", "CHANNEL_UNBRIDGE" -> CallState.BRIDGED;
            case "CHANNEL_HANGUP", "CHANNEL_DESTROY" -> CallState.ENDED;
            default -> null;
        };
    }

    private String inferDirection(NormalizedEslEvent eslEvent) {
        if (eslEvent == null || eslEvent.getChannelVars() == null) {
            return "INBOUND";
        }
        String direction = eslEvent.getChannelVars().get("direction");
        return isBlank(direction) ? "INBOUND" : direction.toUpperCase();
    }

    private String toRawHeaderJson(Map<String, String> rawHeaders) {
        if (rawHeaders == null || rawHeaders.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(rawHeaders);
        } catch (JsonProcessingException e) {
            StringJoiner joiner = new StringJoiner(",", "{", "}");
            rawHeaders.forEach((key, value) -> joiner.add(key + "=" + value));
            return joiner.toString();
        }
    }

    private String buildDedupKey(CallEvent event) {
        return buildDedupKey(event.getCallId(), event.getEventType(), event.getFreeswitchUuid(), event.getEventTime());
    }

    private String buildDedupKey(String callId, String eventType, String freeswitchUuid, LocalDateTime eventTime) {
        return String.join("|",
                valueOrDefault(callId, "NO_CALL"),
                valueOrDefault(eventType, "NO_TYPE"),
                valueOrDefault(freeswitchUuid, "NO_UUID"),
                eventTime == null ? "NO_TIME" : eventTime.toString());
    }

    private String valueOrDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
