package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.entity.OchDataSources;
import com.och.calltask.domain.query.DataSourceQuery;
import com.och.calltask.domain.vo.DataSourceVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源管理表(OchDataSources)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Repository()
@Mapper
public interface OchDataSourcesMapper extends BaseMapper<OchDataSources> {

    List<DataSourceVo> getList(DataSourceQuery query);
}

