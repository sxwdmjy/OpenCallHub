package com.och.call.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.call.domain.ServiceRecord;
import org.apache.ibatis.annotations.Mapper;

/** 服务记录数据访问层 */
@Mapper
public interface ServiceRecordMapper extends BaseMapper<ServiceRecord> {
}
