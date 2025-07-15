package com.och.calltask.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.calltask.mapper.CustomerCrowdRelMapper;
import com.och.calltask.domain.entity.CustomerCrowdRel;
import com.och.calltask.service.ICustomerCrowdRelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 人群客户关联表(CustomerCrowdRel)表服务实现类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Service
public class CustomerCrowdRelServiceImpl extends BaseServiceImpl<CustomerCrowdRelMapper, CustomerCrowdRel> implements ICustomerCrowdRelService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpsert(List<Long> crowdIds, List<CustomerCrowdRel> relList) {
        remove(new LambdaUpdateWrapper<CustomerCrowdRel>().eq(CustomerCrowdRel::getCrowdId,crowdIds));
        saveBatch(relList);
    }

}

