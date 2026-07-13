package com.och.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.och.api.service.IFsXmlCdrService;
import com.och.call.domain.CallRecord;
import com.och.call.mapper.CallRecordMapper;
import com.och.common.exception.ParserException;
import com.och.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FreeSWITCH CDR 处理服务实现类
 * <p>接收 FreeSWITCH 推送的通话详细记录（CDR），解析并持久化到数据库。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FsXmlCdrServiceImpl implements IFsXmlCdrService {

    private final CallRecordMapper callRecordMapper;

    private static final DateTimeFormatter STAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter STAMP_MICRO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    /**
     * 处理 CDR 回调数据
     * <p>解析 FreeSWITCH 推送的 CDR XML 数据，提取通话时长、挂断原因等信息，写入 call_record 表。</p>
     *
     * @param reqText FreeSWITCH 推送的 CDR XML 字符串
     * @throws ParserException 当 XML 解析失败时抛出
     */
    @Override
    public void cdrHandler(String reqText) throws ParserException {
        if (StringUtils.isBlank(reqText)) {
            log.warn("[FsXmlCdrService] 收到空的 CDR 数据");
            return;
        }

        log.info("[FsXmlCdrService] 收到 CDR 数据: length={}", reqText.length());

        try {
            JsonNode root = XML_MAPPER.readTree(reqText);
            Map<String, String> variables = extractVariables(root);

            if (variables.isEmpty()) {
                log.warn("[FsXmlCdrService] CDR XML 中未提取到变量数据");
                return;
            }

            CallRecord record = buildCallRecord(variables);
            callRecordMapper.insert(record);
            log.info("[FsXmlCdrService] CDR 记录已保存: uuid={}, direction={}, talkSeconds={}, endCause={}",
                    record.getCallId(), record.getDirection(), record.getTalkSeconds(), record.getEndCause());
        } catch (IOException e) {
            log.error("[FsXmlCdrService] CDR XML 解析失败", e);
            throw new ParserException("CDR XML 解析失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[FsXmlCdrService] 处理 CDR 数据异常", e);
            throw new ParserException("CDR 处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 CDR XML 中提取所有 channel variables。
     * <p>兼容 FreeSWITCH xml_cdr 的两种常见格式：</p>
     * <ul>
     *   <li>属性格式: {@code <variable name="k" value="v"/>}</li>
     *   <li>子元素格式: {@code <k>v</k>}</li>
     * </ul>
     * <p>会从 {@code <variables>} 和 {@code <channel_data>} 两个区段收集字段。</p>
     */
    private Map<String, String> extractVariables(JsonNode root) {
        Map<String, String> vars = new LinkedHashMap<>();

        // 从 <variables> 区段提取
        if (root.has("variables")) {
            extractSection(root.get("variables"), vars);
        }

        // 从 <channel_data> 区段提取（部分模板会在此放置 uuid / state / direction 等）
        if (root.has("channel_data")) {
            extractSection(root.get("channel_data"), vars);
        }

        // 兜底：直接从根元素提取（如果 CDR XML 没有嵌套区段）
        if (vars.isEmpty()) {
            extractSection(root, vars);
        }

        return vars;
    }

    /**
     * 从一个 JsonNode 区段中提取 key-value 变量。
     */
    private void extractSection(JsonNode section, Map<String, String> vars) {
        // 格式 1: <variable name="k" value="v"/> (属性格式)
        if (section.has("variable")) {
            JsonNode varList = section.get("variable");
            if (varList.isArray()) {
                for (JsonNode var : varList) {
                    extractAttributeVar(var, vars);
                }
            } else {
                extractAttributeVar(varList, vars);
            }
        }

        // 格式 2: <uuid>abc</uuid> / <duration>10</duration> (子元素格式)
        section.fieldNames().forEachRemaining(fieldName -> {
            if ("variable".equals(fieldName)) {
                return;
            }
            JsonNode child = section.get(fieldName);
            if (child.isValueNode()) {
                vars.putIfAbsent(fieldName, child.asText());
            }
        });
    }

    /**
     * 解析单个 {@code <variable name="k" value="v"/>} 节点。
     */
    private void extractAttributeVar(JsonNode varNode, Map<String, String> vars) {
        if (varNode.has("name") && varNode.has("value")) {
            vars.put(varNode.get("name").asText(), varNode.get("value").asText());
        }
    }

    /**
     * 将提取的变量映射为 CallRecord 实体。
     */
    private CallRecord buildCallRecord(Map<String, String> vars) {
        String uuid = vars.get("uuid");
        String callerNumber = vars.get("caller_id_number");
        String calledNumber = vars.get("destination_number");
        String direction = vars.get("direction");
        String hangupCause = vars.get("hangup_cause");
        String durationStr = vars.get("duration");
        String billsecStr = vars.get("billsec");
        String startStamp = vars.get("start_stamp");
        String endStamp = vars.get("end_stamp");
        String recordingUrl = vars.get("recording_url");

        Integer duration = parseInteger(durationStr);
        Integer billsec = parseInteger(billsecStr);

        int talkSeconds = billsec != null ? billsec : 0;
        int ringSeconds = 0;
        if (duration != null && billsec != null) {
            ringSeconds = Math.max(0, duration - billsec);
        }

        String state = deriveState(billsecStr, hangupCause);

        return CallRecord.builder()
                .callId(uuid)
                .callerNumber(callerNumber)
                .calledNumber(calledNumber)
                .direction(direction)
                .state(state)
                .talkSeconds(talkSeconds)
                .ringSeconds(ringSeconds)
                .endCause(hangupCause)
                .recordingUrl(recordingUrl)
                .startTime(parseDateTime(startStamp))
                .endTime(parseDateTime(endStamp))
                .build();
    }

    /**
     * 根据 billsec 和 hangup_cause 推导通话最终状态。
     */
    private String deriveState(String billsecStr, String hangupCause) {
        Integer billsec = parseInteger(billsecStr);
        // 已应答：billsec > 0 或有通话时长
        if (billsec != null && billsec > 0) {
            return "ANSWERED";
        }
        if (StringUtils.isBlank(hangupCause)) {
            return "UNKNOWN";
        }
        String cause = hangupCause.toUpperCase();
        if (cause.contains("NO_ANSWER") || cause.contains("NO_PICKUP")) {
            return "NO_ANSWER";
        }
        if (cause.contains("USER_BUSY") || cause.contains("BUSY")) {
            return "BUSY";
        }
        if (cause.contains("CANCEL") || cause.contains("ORIGINATOR_CANCEL")) {
            return "CANCEL";
        }
        if (cause.contains("NORMAL_CLEARING") || cause.contains("SUCCESS")) {
            // billsec 为 0 但原因是正常清除，说明未接通
            return "NO_ANSWER";
        }
        if (cause.contains("DESTINATION_OUT_OF_ORDER") || cause.contains("UNALLOCATED")) {
            return "FAILED";
        }
        return hangupCause;
    }

    /**
     * 安全解析整数字符串。
     */
    private Integer parseInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.debug("[FsXmlCdrService] 无法解析整数值: {}", value);
            return null;
        }
    }

    /**
     * 将 FreeSWITCH 时间戳字符串解析为 LocalDateTime。
     * <p>兼容格式:</p>
     * <ul>
     *   <li>"yyyy-MM-dd HH:mm:ss"</li>
     *   <li>"yyyy-MM-dd HH:mm:ss.SSSSSS"（带微秒）</li>
     *   <li>ISO_LOCAL_DATE_TIME</li>
     * </ul>
     */
    private LocalDateTime parseDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            if (trimmed.contains(".")) {
                return LocalDateTime.parse(trimmed, STAMP_MICRO_FORMATTER);
            }
            return LocalDateTime.parse(trimmed, STAMP_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                log.debug("[FsXmlCdrService] 无法解析时间戳: {}", value);
                return null;
            }
        }
    }
}
