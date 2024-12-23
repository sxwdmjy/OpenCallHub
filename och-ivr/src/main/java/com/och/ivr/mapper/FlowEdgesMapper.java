package com.och.ivr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.ivr.domain.FlowEdges;

/**
 * 存储流程中节点之间的连接信息（流转规则）(FlowEdges)表数据库访问层
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Repository()
@Mapper
public interface FlowEdgesMapper extends BaseMapper<FlowEdges> {

}

