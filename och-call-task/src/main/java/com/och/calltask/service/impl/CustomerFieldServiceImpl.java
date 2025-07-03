package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.entity.CustomerField;
import com.och.calltask.domain.query.CustomerFieldAddQuery;
import com.och.calltask.domain.query.CustomerFieldQuery;
import com.och.calltask.domain.vo.CustomerFieldVo;
import com.och.calltask.mapper.CustomerFieldMapper;
import com.och.calltask.service.ICustomerFieldService;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.common.utils.StringUtils;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 客户字段管理表(CustomerField)表服务实现类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@RequiredArgsConstructor
@Service
public class CustomerFieldServiceImpl extends BaseServiceImpl<CustomerFieldMapper, CustomerField> implements ICustomerFieldService {

    private final ISysUserService sysUserService;

    @Override
    public void add(CustomerFieldAddQuery query) {
        Boolean checkFieldName = checkFieldName(query.getFieldName());
        if (checkFieldName) {
            throw new CommonException("字段名称已存在");
        }
        CustomerField customerField = new CustomerField();
        BeanUtils.copyProperties(query, customerField);
        save(customerField);
    }


    @Override
    public void edit(CustomerFieldAddQuery query) {
        CustomerField field = getById(query.getId());
        if (Objects.isNull(field)) {
            throw new CommonException("无效ID");
        }
        if (!StringUtils.equals(field.getFieldName(), query.getFieldName()) && checkFieldName(query.getFieldName())) {
            throw new CommonException("字段名称已存在");
        } else if (StringUtils.isNotBlank(query.getFieldName())) {
            field.setFieldName(query.getFieldName());
        }
        if (StringUtils.isNotBlank(query.getFieldLabel())) {
            field.setFieldLabel(query.getFieldLabel());
        }
        if (Objects.nonNull(query.getFieldType())) {
            field.setFieldType(query.getFieldType());
        }
        if (Objects.nonNull(query.getRequired())) {
            field.setRequired(query.getRequired());
        }
        if (StringUtils.isNotBlank(query.getOptions())) {
            field.setOptions(query.getOptions());
        }
        if (Objects.nonNull(query.getStatus())) {
            field.setStatus(query.getStatus());
        }
        updateById(field);
    }

    @Override
    public CustomerFieldVo getDetail(Long id) {
        CustomerField field = getById(id);
        if (Objects.isNull(field)) {
            throw new CommonException("无效ID");
        }
        CustomerFieldVo fieldVo = new CustomerFieldVo();
        BeanUtils.copyProperties(field, fieldVo);
        return fieldVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(CustomerFieldQuery query) {
        List<Long> ids = new LinkedList<>();
        if (Objects.nonNull(query.getId())) {
            ids.add(query.getId());
        }
        if (CollectionUtil.isNotEmpty(query.getIdList())) {
            ids.addAll(query.getIdList());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        List<CustomerField> list = ids.stream().map(id -> {
            CustomerField fieldInfo = new CustomerField();
            fieldInfo.setId(id);
            fieldInfo.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return fieldInfo;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public List<CustomerFieldVo> pageList(CustomerFieldQuery query) {
        super.startPage(query.getPageIndex(), query.getPageSize());
        List<CustomerFieldVo> customerFields = getList(query);
        sysUserService.decorate(customerFields);
        return customerFields;
    }

    @Override
    public List<CustomerFieldVo> getList(CustomerFieldQuery query) {
        return this.baseMapper.getList(query);
    }

    private Boolean checkFieldName(String fieldName) {
        long count = count(new LambdaQueryWrapper<CustomerField>().eq(CustomerField::getFieldName, fieldName).eq(BaseEntity::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        return count > 0;
    }
}

