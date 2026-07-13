package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.RobotFlow;

/**
 * 机器人流程 Mapper
 * <p>提供 robot_flow 表的基础 CRUD 操作，包括新增、修改、删除、查询等。
 * robot_flow 表存储 AI 机器人对话流程的配置信息，包括 bot 引擎类型（DIFY/INTERNAL/RAGFLOW/LANGFLOW）、
 * 默认问候语、最大轮次、静默超时和转人工条件等核心配置。</p>
 */
public interface RobotFlowMapper extends BaseMapper<RobotFlow> {

}
