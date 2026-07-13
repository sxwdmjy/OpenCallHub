package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.PluginCallLog;

/**
 * 插件调用日志 Mapper
 * <p>提供 plugin_call_log 表的基础 CRUD 操作。
 * plugin_call_log 表记录每个 AI 插件（ASR/TTS/LLM）的调用日志，
 * 包括请求数据、响应数据、延迟、是否成功、错误信息和费用等详细调用信息。</p>
 */
public interface PluginCallLogMapper extends BaseMapper<PluginCallLog> {

}
