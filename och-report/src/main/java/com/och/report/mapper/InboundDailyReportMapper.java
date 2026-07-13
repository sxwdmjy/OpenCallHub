package com.och.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.report.domain.InboundDailyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 呼入日报 Mapper，基于 MyBatis-Plus BaseMapper。
 * 可通过 LambdaQueryWrapper 按日期范围查询呼入日报数据。
 */
@Mapper
public interface InboundDailyReportMapper extends BaseMapper<InboundDailyReport> {
}
