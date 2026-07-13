package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.RobotIntent;

/**
 * 机器人意图 Mapper
 * <p>提供 robot_intent 表的基础 CRUD 操作。
 * robot_intent 表存储机器人流程中定义的意图信息，包括意图名称、编码、
 * 示例语句、回复模板和触发动作等配置。</p>
 */
public interface RobotIntentMapper extends BaseMapper<RobotIntent> {

}
