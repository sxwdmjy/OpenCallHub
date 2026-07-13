package com.och.ai.api;

import com.och.ai.service.impl.AiPluginInvokeServiceImpl;
import com.och.common.api.ApiResponse;
import com.och.common.base.BaseController;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 插件调用接口
 * <p>统一暴露 ASR、TTS 和 LLM 能力，实际供应商调用由应用服务和插件适配器完成。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Tag(name = "AI", description = "AI插件服务")
@RestController("aiPluginController")
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiPluginController extends BaseController {

    private final AiPluginInvokeServiceImpl aiPluginInvokeService;

    @Operation(summary = "ASR: 语音转文字")
    @PostMapping("/asr")
    @Audited(action = "AI_ASR", targetType = "AI_PLUGIN")
    public ApiResponse<AiPluginInvokeServiceImpl.AsrResult> asr(@RequestParam String audioData,
                                                            @RequestParam(defaultValue = "default") String pluginCode) {
        return success(aiPluginInvokeService.invokeAsr(audioData, pluginCode));
    }

    @Operation(summary = "TTS: 文字转语音")
    @PostMapping("/tts")
    @Audited(action = "AI_TTS", targetType = "AI_PLUGIN")
    public ApiResponse<AiPluginInvokeServiceImpl.TtsResult> tts(@RequestParam String text,
                                                            @RequestParam(defaultValue = "default") String voice,
                                                            @RequestParam(defaultValue = "default") String pluginCode) {
        return success(aiPluginInvokeService.invokeTts(text, voice, pluginCode));
    }

    @Operation(summary = "LLM: 对话生成")
    @PostMapping("/llm")
    @Audited(action = "AI_LLM", targetType = "AI_PLUGIN")
    public ApiResponse<AiPluginInvokeServiceImpl.LlmResult> llm(@RequestParam String prompt,
                                                            @RequestParam(defaultValue = "") String context,
                                                            @RequestParam(defaultValue = "default") String pluginCode) {
        return success(aiPluginInvokeService.invokeLlm(prompt, context, pluginCode));
    }
}
