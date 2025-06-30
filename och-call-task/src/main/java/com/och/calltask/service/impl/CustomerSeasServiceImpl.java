package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.och.calltask.domain.entity.CustomerSeas;
import com.och.calltask.domain.query.CustomerSeasAddQuery;
import com.och.calltask.domain.query.CustomerSeasQuery;
import com.och.calltask.domain.vo.CustomerSeasVo;
import com.och.calltask.mapper.CustomerSeasMapper;
import com.och.calltask.service.ICustomerSeasService;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 客户公海表(CustomerSeas)表服务实现类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@RequiredArgsConstructor
@Service
public class CustomerSeasServiceImpl extends BaseServiceImpl<CustomerSeasMapper, CustomerSeas> implements ICustomerSeasService {

    private final ISysUserService sysUserService;

    @Override
    public void add(CustomerSeasAddQuery query) {
        CustomerSeas customerSeas = new CustomerSeas();
        BeanUtils.copyProperties(query, customerSeas);
        save(customerSeas);
    }

    @Override
    public void edit(CustomerSeasAddQuery query) {
        CustomerSeas customerSeas = getById(query.getId());
        if (Objects.isNull(customerSeas)) {
            throw new CommonException("无效ID");
        }
        BeanUtils.copyProperties(query, customerSeas);
        updateById(customerSeas);
    }

    @Override
    public CustomerSeasVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public void delete(CustomerSeasQuery query) {
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
        List<CustomerSeas> list = ids.stream().map(id -> {
            CustomerSeas seas = new CustomerSeas();
            seas.setId(id);
            seas.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return seas;
        }).toList();
        updateBatchById(list);
    }

    @Override
    public List<CustomerSeasVo> pageList(CustomerSeasQuery query) {
        super.startPage(query.getPageIndex(), query.getPageSize());
        List<CustomerSeasVo> customerSeas =getList(query);
        if(!CollectionUtil.isEmpty(customerSeas)){
            sysUserService.decorate(customerSeas);
        }
        return customerSeas;
    }

    @Override
    public List<CustomerSeasVo> getList(CustomerSeasQuery query) {
        return this.baseMapper.getList(query);
    }
}

