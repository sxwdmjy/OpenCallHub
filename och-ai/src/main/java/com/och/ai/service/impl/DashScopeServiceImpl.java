package com.och.ai.service.impl;


import com.och.ai.service.IDashScopeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author danmo
 * @date 2025/06/10 16:14
 */
@AllArgsConstructor
@Slf4j
@Service
public class DashScopeServiceImpl implements IDashScopeService {

    private final ChatClient dashscopeClient;


    @Override
    public String chat(String question) {
        return dashscopeClient.prompt(new Prompt(question)).call().content();
    }

    @Override
    public String chat(String chatId, String question) {
        return dashscopeClient.prompt(new Prompt(question))
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call().content();
    }
}
