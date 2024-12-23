package com.och.ivr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.ivr.domain.FlowInfo;

/**
 * ivr流程信息(FlowInfo)表数据库访问层
 *
 * @author danmo
 * @since 2024-12-23 15:08:24
 */
@Repository()
@Mapper
public interface FlowInfoMapper extends BaseMapper<FlowInfo> {

}

