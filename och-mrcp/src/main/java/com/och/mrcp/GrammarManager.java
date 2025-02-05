package com.och.mrcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GrammarManager {
    private static final Map<String, Map<String, String>> sessionGrammars = new ConcurrentHashMap<>();

    /**
     * 存储语法到会话
     */
    public static void storeGrammar(String sessionId, String grammarId, String grammarXml) {
        sessionGrammars
                .computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                .put(grammarId, grammarXml);
    }

    /**
     * 获取会话中的语法
     */
    public static String getGrammar(String sessionId, String grammarId) {
        return sessionGrammars
                .getOrDefault(sessionId, Map.of())
                .get(grammarId);
    }

    /**
     * 删除会话中的语法
     */
    public static void removeGrammar(String sessionId, String grammarId) {
        sessionGrammars
                .getOrDefault(sessionId, Map.of())
                .remove(grammarId);
    }
}