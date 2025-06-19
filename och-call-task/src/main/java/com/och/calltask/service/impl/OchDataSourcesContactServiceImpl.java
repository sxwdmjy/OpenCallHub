package com.och.calltask.service.impl;

import com.och.calltask.domain.entity.OchDataSourcesContact;
import com.och.calltask.domain.query.DataSourceContactQuery;
import com.och.calltask.domain.vo.DataSourcesContactVo;
import com.och.calltask.mapper.OchDataSourcesContactMapper;
import com.och.calltask.service.IOchDataSourcesContactService;
import com.och.common.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据源联系人表(OchDataSourcesContact)表服务实现类
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Service
public class OchDataSourcesContactServiceImpl extends BaseServiceImpl<OchDataSourcesContactMapper, OchDataSourcesContact> implements IOchDataSourcesContactService {


    @Override
    public List<DataSourcesContactVo> getContactList(DataSourceContactQuery query) {
        return this.baseMapper.getContactList(query);
    }
}

