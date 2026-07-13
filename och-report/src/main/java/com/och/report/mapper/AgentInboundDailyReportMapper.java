package com.och.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.report.domain.AgentInboundDailyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 坐席呼入日报 Mapper，基于 MyBatis-Plus BaseMapper。
 * 可通过 LambdaQueryWrapper 按坐席ID和日期查询坐席绩效数据。
 */
@Mapper
public interface AgentInboundDailyReportMapper extends BaseMapper<AgentInboundDailyReport> {
}
