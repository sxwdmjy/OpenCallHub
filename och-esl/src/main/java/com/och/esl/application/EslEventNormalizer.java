package com.och.esl.application;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import com.och.esl.application.EslEventNormalizer;
import lombok.extern.slf4j.Slf4j;
import com.och.esl.application.EslEventNormalizer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ESL 事件规范化服务。
 * <p>
 * 将 FreeSWITCH 原始事件头转换为内部稳定的 {@link NormalizedEslEvent}。
 * 本类只做协议适配：事件名映射、关键字段提取、时间解析和原始头裁剪，
 * 不判断 IVR、ACD 或外呼业务走向，确保 `och-esl` 只承担通信边界职责。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
public class EslEventNormalizer{

    private static final DateTimeFormatter FS_LOCAL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** rawHeaders 排障保留字段，避免高并发下把全部 ESL 头长期写入内存和数据库。 */
    private static final Set<String> RAW_HEADER_KEEP_KEYS = Set.of(
            "Event-Name", "Event-Date-Local", "Event-Date-Timestamp", "Unique-ID", "Other-Leg-Unique-ID",
            "Caller-Unique-ID", "Caller-Caller-ID-Number", "Caller-Destination-Number", "DTMF-Digit",
            "Hangup-Cause", "Channel-State", "Answer-State", "FreeSWITCH-IPv4"
    );

    /**
     * 将 FreeSWITCH 原始事件头规范化为标准事件对象。
     *
     * @param rawHeaders FreeSWITCH 原始事件头键值对
     * @return 标准化 ESL 事件
     */
    public NormalizedEslEvent normalize(Map<String, String> rawHeaders) {
        Map<String, String> headers = rawHeaders == null ? Map.of() : rawHeaders;
        Map<String, String> channelVars = extractChannelVars(headers);
        EslEventType type = mapEventType(headers.getOrDefault("Event-Name", "CUSTOM"));
        String currentUuid = firstNonBlank(headers.get("Unique-ID"), headers.get("Channel-Unique-ID"));

        NormalizedEslEvent event = NormalizedEslEvent.builder()
                .callId(extractCallId(headers, channelVars, currentUuid))
                .eventType(type)
                .eventTime(extractEventTime(headers))
                .freeswitchUuid(currentUuid)
                .legId(currentUuid)
                .otherLegId(headers.get("Other-Leg-Unique-ID"))
                .callerNumber(firstNonBlank(headers.get("Caller-Caller-ID-Number"), headers.get("variable_caller_id_number")))
                .calledNumber(firstNonBlank(headers.get("Caller-Destination-Number"), headers.get("variable_destination_number")))
                .dtmfDigit(headers.get("DTMF-Digit"))
                .hangupCause(headers.get("Hangup-Cause"))
                .channelState(headers.get("Channel-State"))
                .answerState(headers.get("Answer-State"))
                .sipCallId(channelVars.get("sip_call_id"))
                .channelVars(channelVars)
                .rawHeaders(clipRawHeaders(headers))
                .build();

        log.debug("[EslEventNormalizer] 规范化: type={}, callId={}, uuid={}, caller={}, called={}, state={}",
                type, event.getCallId(), event.getFreeswitchUuid(),
                EslUtils.maskPhone(event.getCallerNumber()), EslUtils.maskPhone(event.getCalledNumber()),
                event.getChannelState());
        return event;
    }

    private Map<String, String> extractChannelVars(Map<String, String> headers) {
        return headers.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().startsWith("variable_"))
                .collect(Collectors.toMap(
                        e -> e.getKey().substring("variable_".length()),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new));
    }

    /**
     * 提取业务呼叫标识。
     * <p>
     * `variable_call_id` 是业务系统写入的稳定标识，优先级最高；
     * `Caller-Unique-ID` 在 FreeSWITCH A-leg 生命周期内较稳定；最后才退回当前通道 UUID。
     * </p>
     */
    private String extractCallId(Map<String, String> headers, Map<String, String> channelVars, String currentUuid) {
        return firstNonBlank(
                channelVars.get("call_id"),
                headers.get("variable_call_id"),
                headers.get("Caller-Unique-ID"),
                currentUuid
        );
    }

    /**
     * 提取 FreeSWITCH 事件时间，优先使用微秒时间戳，避免本地接收时间造成事件乱序。
     */
    private LocalDateTime extractEventTime(Map<String, String> headers) {
        String timestamp = headers.get("Event-Date-Timestamp");
        if (timestamp != null && !timestamp.isBlank()) {
            try {
                long micros = Long.parseLong(timestamp.trim());
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(micros / 1000), ZoneId.systemDefault());
            } catch (NumberFormatException e) {
                log.debug("[EslEventNormalizer] Event-Date-Timestamp 解析失败: {}", timestamp);
            }
        }

        String localTime = headers.get("Event-Date-Local");
        if (localTime != null && localTime.length() >= 19) {
            try {
                return LocalDateTime.parse(localTime.substring(0, 19), FS_LOCAL_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                log.debug("[EslEventNormalizer] Event-Date-Local 解析失败: {}", localTime);
            }
        }
        return LocalDateTime.now();
    }

    /**
     * 裁剪原始事件头，只保留排障所需关键字段和少量业务变量。
     */
    private Map<String, String> clipRawHeaders(Map<String, String> headers) {
        Map<String, String> clipped = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            if (key == null || value == null) {
                return;
            }
            if (RAW_HEADER_KEEP_KEYS.contains(key) || key.startsWith("variable_call_") || key.startsWith("variable_sip_")) {
                clipped.put(key, value.length() > 512 ? value.substring(0, 512) : value);
            }
        });
        return clipped;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * 将 FreeSWITCH 事件名称映射为内部事件类型枚举。
     */
    private EslEventType mapEventType(String eventName) {
        String safeEventName = eventName == null ? "CUSTOM" : eventName;
        return switch (safeEventName.toUpperCase()) {
            case "CHANNEL_CREATE"           -> EslEventType.CHANNEL_CREATE;
            case "CHANNEL_OUTGOING"         -> EslEventType.CHANNEL_OUTGOING;
            case "CHANNEL_ORIGINATE"        -> EslEventType.CHANNEL_ORIGINATE;
            case "CHANNEL_PROGRESS"         -> EslEventType.CHANNEL_PROGRESS;
            case "CHANNEL_PROGRESS_MEDIA"   -> EslEventType.CHANNEL_PROGRESS_MEDIA;
            case "CHANNEL_ANSWER"           -> EslEventType.CHANNEL_ANSWER;
            case "CHANNEL_BRIDGE"           -> EslEventType.CHANNEL_BRIDGE;
            case "CHANNEL_UNBRIDGE"         -> EslEventType.CHANNEL_UNBRIDGE;
            case "CHANNEL_HOLD"             -> EslEventType.CHANNEL_HOLD;
            case "CHANNEL_UNHOLD"           -> EslEventType.CHANNEL_UNHOLD;
            case "CHANNEL_PARK"             -> EslEventType.CHANNEL_PARK;
            case "CHANNEL_UNPARK"           -> EslEventType.CHANNEL_UNPARK;
            case "CHANNEL_HANGUP", "CHANNEL_HANGUP_COMPLETE" -> EslEventType.CHANNEL_HANGUP;
            case "CHANNEL_EXECUTE"          -> EslEventType.CHANNEL_EXECUTE;
            case "CHANNEL_EXECUTE_COMPLETE" -> EslEventType.CHANNEL_EXECUTE_COMPLETE;
            case "CHANNEL_DESTROY"          -> EslEventType.CHANNEL_DESTROY;
            case "DTMF"                     -> EslEventType.DTMF;
            case "RECORD_START"             -> EslEventType.RECORD_START;
            case "RECORD_STOP"              -> EslEventType.RECORD_STOP;
            case "CODEC"                    -> EslEventType.CODEC;
            case "HEARTBEAT"                -> EslEventType.HEARTBEAT;
            default                          -> EslEventType.CUSTOM;
        };
    }
}
