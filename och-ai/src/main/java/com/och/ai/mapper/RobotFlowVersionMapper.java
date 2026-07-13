package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.RobotFlowVersion;

/**
 * 机器人流程版本 Mapper
 * <p>提供 robot_flow_version 表的基础 CRUD 操作。
 * robot_flow_version 表存储机器人流程的版本快照，每次发布都会生成一条新记录。
 * 包含 LLM、ASR、TTS 插件配置、系统提示词、意图定义、槽位定义和转人工规则等完整配置。</p>
 */
public interface RobotFlowVersionMapper extends BaseMapper<RobotFlowVersion> {

}
