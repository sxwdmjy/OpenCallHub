package com.och.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.report.domain.OutboundDailyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 外呼日报 Mapper，基于 MyBatis-Plus BaseMapper。
 * 可按任务ID和日期范围查询外呼日报数据。
 */
@Mapper
public interface OutboundDailyReportMapper extends BaseMapper<OutboundDailyReport> {
}
