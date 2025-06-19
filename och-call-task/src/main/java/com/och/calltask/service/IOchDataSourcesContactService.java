package com.och.calltask.service;

import com.och.calltask.domain.entity.OchDataSourcesContact;
import com.och.calltask.domain.query.DataSourceContactQuery;
import com.och.calltask.domain.vo.DataSourcesContactVo;
import com.och.common.base.IBaseService;

import java.util.List;

/**
 * 数据源联系人表(OchDataSourcesContact)表服务接口
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
public interface IOchDataSourcesContactService extends IBaseService<OchDataSourcesContact> {

    /**
     * 通过数据源ID查询联系人列表
     * @param query  查询参数
     * @return  联系人列表
     */
    List<DataSourcesContactVo> getContactList(DataSourceContactQuery query);
}

