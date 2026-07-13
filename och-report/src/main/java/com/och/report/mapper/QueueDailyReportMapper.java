package com.och.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.report.domain.QueueDailyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 队列日报 Mapper，基于 MyBatis-Plus BaseMapper。
 * 可按技能组ID和日期范围查询队列SLA日报数据。
 */
@Mapper
public interface QueueDailyReportMapper extends BaseMapper<QueueDailyReport> {
}
