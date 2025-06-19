package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.entity.OchDataSourcesContact;
import com.och.calltask.domain.query.DataSourceContactQuery;
import com.och.calltask.domain.vo.DataSourcesContactVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

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

