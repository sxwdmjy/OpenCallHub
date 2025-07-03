package com.och.calltask.service.impl;

import com.och.common.base.BaseServiceImpl;
import com.och.calltask.mapper.CustomerCrowdRelMapper;
import com.och.calltask.domain.entity.CustomerCrowdRel;
import com.och.calltask.service.ICustomerCrowdRelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 人群客户关联表(CustomerCrowdRel)表服务实现类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Service
public class CustomerCrowdRelServiceImpl extends BaseServiceImpl<CustomerCrowdRelMapper, CustomerCrowdRel> implements ICustomerCrowdRelService {

    @Override
    public void batchUpsert(List<CustomerCrowdRel> relList) {
        this.baseMapper.batchUpsert(relList);
    }
}

