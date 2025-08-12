package com.och.ai.service.impl;


import com.och.ai.service.IDeepSeekService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * @author danmo
 * @date 2025/06/10 16:14
 */
@AllArgsConstructor
@Slf4j
@Service
public class DeepSeekServiceImpl implements IDeepSeekService {

    private final ChatClient deepseekClient;

    @Override
    public String chat(String question) {
        return deepseekClient.prompt(new Prompt(question)).call().content();
    }
}
