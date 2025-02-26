package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.query.engine.CallEngineQuery;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.CallEngine;

import java.util.List;

/**
 * AI引擎表(CallEngine)表数据库访问层
 *
 * @author danmo
 * @since 2025-02-24 14:49:07
 */
@Repository()
@Mapper
public interface CallEngineMapper extends BaseMapper<CallEngine> {

    List<CallEngine> getList(CallEngineQuery query);
}

