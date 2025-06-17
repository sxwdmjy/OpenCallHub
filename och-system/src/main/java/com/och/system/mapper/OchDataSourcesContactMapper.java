package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.query.calltask.DataSourceContactQuery;
import com.och.system.domain.vo.calltask.DataSourcesContactVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.OchDataSourcesContact;

import java.util.List;

/**
 * 数据源联系人表(OchDataSourcesContact)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Repository()
@Mapper
public interface OchDataSourcesContactMapper extends BaseMapper<OchDataSourcesContact> {

    List<DataSourcesContactVo> getContactList(DataSourceContactQuery query);
}

