package com.och.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.report.domain.AiRobotDailyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI机器人日报 Mapper，基于 MyBatis-Plus BaseMapper。
 * 可按日期范围查询AI机器人日报数据。
 */
@Mapper
public interface AiRobotDailyReportMapper extends BaseMapper<AiRobotDailyReport> {
}
