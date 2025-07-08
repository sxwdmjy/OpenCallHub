package com.och.ai.service;


/**
 * @author danmo
 * @date 2025/06/10 16:14
 */
public interface IDashScopeService {

    String chat(String question);

    String chat(String chatId, String question);
}
