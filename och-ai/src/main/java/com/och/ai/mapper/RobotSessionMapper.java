package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.RobotSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 机器人会话 Mapper
 * <p>提供 robot_session 表的基础 CRUD 操作。
 * robot_session 表存储 AI 机器人通话会话的运行时状态，包括会话状态、当前意图、
 * 槽位值、会话摘要、标签、各环节累计延迟等关键信息。</p>
 */
@Mapper
public interface RobotSessionMapper extends BaseMapper<RobotSession> {

}
