package com.och.ivr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ivr.domain.IvrFlow;
import org.apache.ibatis.annotations.Mapper;

/**
 * IVR流程数据访问层
 * <p>
 * 提供IVR流程（ivr_flow表）的CRUD数据库操作，
 * 基于 MyBatis-Plus BaseMapper 自动生成基础SQL。
 * </p>
 */
@Mapper
public interface IvrFlowMapper extends BaseMapper<IvrFlow> {
}
