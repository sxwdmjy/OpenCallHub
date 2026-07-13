package com.och.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ticket.domain.Ticket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单 Mapper，基于 MyBatis-Plus BaseMapper。
 * <p>
 * 继承 BaseMapper 后自动拥有 CRUD 基础能力，无需手写 SQL。
 * 如需复杂查询可通过 {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 构建条件。
 * </p>
 *
 * @author danmo
 */
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}
