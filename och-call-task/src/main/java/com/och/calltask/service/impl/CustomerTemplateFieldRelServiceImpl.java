package com.och.calltask.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.och.calltask.domain.query.CustomerTemplateFieldRelAddQuery;
import com.och.common.base.BaseServiceImpl;
import com.och.calltask.mapper.CustomerTemplateFieldRelMapper;
import com.och.calltask.domain.entity.CustomerTemplateFieldRel;
import com.och.calltask.service.ICustomerTemplateFieldRelService;
import com.och.common.enums.DeleteStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 客户模板字段关联表(CustomerTemplateFieldRel)表服务实现类
 *
 * @author danmo
 * @since 2025-06-30 11:35:45
 */
@Service
public class CustomerTemplateFieldRelServiceImpl extends BaseServiceImpl<CustomerTemplateFieldRelMapper, CustomerTemplateFieldRel> implements ICustomerTemplateFieldRelService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveByTemplateId(Long templateId, List<CustomerTemplateFieldRelAddQuery> fieldList) {
        if(CollectionUtils.isEmpty(fieldList)){
            return;
        }
        List<CustomerTemplateFieldRel> list = fieldList.stream().map(item -> {
            CustomerTemplateFieldRel rel = new CustomerTemplateFieldRel();
            rel.setTemplateId(templateId);
            rel.setFieldId(item.getFieldId());
            rel.setHidden(item.getHidden());
            rel.setSort(item.getSort());
            return rel;
        }).toList();
        saveBatch(list);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByTemplateId(Long templateId, List<CustomerTemplateFieldRelAddQuery> fieldList) {
        deleteByTemplateId(templateId);
        saveByTemplateId(templateId,fieldList);
    }

    @Override
    public void deleteByTemplateId(Long templateId) {
        deleteByTemplateId(List.of(templateId));
    }

    @Override
    public void deleteByTemplateId(List<Long> templateIds) {
        CustomerTemplateFieldRel rel = new CustomerTemplateFieldRel();
        rel.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        update(rel,new LambdaUpdateWrapper<CustomerTemplateFieldRel>().eq(CustomerTemplateFieldRel::getTemplateId,templateIds));
    }
}

