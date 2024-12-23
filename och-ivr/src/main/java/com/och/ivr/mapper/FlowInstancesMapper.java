package com.och.ivr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.ivr.domain.entity.FlowInstances;

/**
 * 存储流程实例的基本信息(FlowInstances)表数据库访问层
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Repository()
@Mapper
public interface FlowInstancesMapper extends BaseMapper<FlowInstances> {

}

