package com.och.mrcp;

import com.och.exception.MrcpParseException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MrcpMessageParser {
    // 正则表达式：匹配请求和响应的起始行
    private static final Pattern REQUEST_START_LINE_PATTERN =
            Pattern.compile("MRCP/(\\d+\\.\\d+)\\s+(\\d+)\\s+([A-Z-]+)\\s+(\\d+)");
    private static final Pattern RESPONSE_START_LINE_PATTERN =
            Pattern.compile("MRCP/(\\d+\\.\\d+)\\s+(\\d{3})\\s+(\\d+)\\s+(.+)");

    public static MrcpMessage parse(String raw) throws MrcpParseException {
        try {
            String[] parts = raw.split("\r\n\r\n", 2);
            String headerSection = parts[0];
            String body = (parts.length > 1) ? parts[1] : null;

            String[] headers = headerSection.split("\r\n");

            // 1. 解析起始行
            String startLine = headers[0];
            MrcpMessage message = createMessage(startLine);

            // 2. 解析头部字段
            parseHeaders(message, headers);

            // 3. 设置正文
            if (body != null) {
                message.setBody(body);
            }

            return message;
        } catch (Exception e) {
            throw new MrcpParseException("Failed to parse MRCP message", e);
        }
    }

    private static MrcpMessage createMessage(String startLine) throws MrcpParseException {
        // 优先匹配响应格式
        Matcher responseMatcher = RESPONSE_START_LINE_PATTERN.matcher(startLine);
        if (responseMatcher.matches()) {
            MrcpResponse response = new MrcpResponse();
            response.setVersion(responseMatcher.group(1));
            response.setStatusCode(Integer.parseInt(responseMatcher.group(2)));
            response.setRequestId(responseMatcher.group(3));
            return response;
        }

        // 若未匹配响应，尝试匹配请求格式
        Matcher requestMatcher = REQUEST_START_LINE_PATTERN.matcher(startLine);
        if (requestMatcher.matches()) {
            MrcpRequest request = new MrcpRequest();
            request.setVersion("MRCP/"+requestMatcher.group(1));
            request.setRequestId(requestMatcher.group(4));
            request.setMethod(requestMatcher.group(3));
            return request;
        }

        throw new MrcpParseException("Invalid MRCP start line: " + startLine);
    }

    private static void parseHeaders(MrcpMessage msg, String[] headers) {
        for (int i = 1; i < headers.length; i++) {
            String headerLine = headers[i].trim();
            if (headerLine.isEmpty()) continue;
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex == -1) {
                log.warn("Invalid header line: {}", headerLine);
                continue;
            }
            String key = headerLine.substring(0, colonIndex).trim();
            String value = headerLine.substring(colonIndex + 1).trim();
            msg.addHeader(key, value);
        }
    }
}