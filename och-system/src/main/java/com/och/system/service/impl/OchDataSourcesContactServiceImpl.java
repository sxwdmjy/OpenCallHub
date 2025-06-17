package com.och.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.query.calltask.DataSourceContactQuery;
import com.och.system.domain.vo.calltask.DataSourcesContactVo;
import com.och.system.mapper.OchDataSourcesContactMapper;
import com.och.system.domain.entity.OchDataSourcesContact;
import com.och.system.service.IOchDataSourcesContactService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

