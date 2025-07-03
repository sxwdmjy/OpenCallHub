package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.entity.CustomerCrowd;
import com.och.calltask.domain.query.CustomerCrowdAddQuery;
import com.och.calltask.domain.query.CustomerCrowdQuery;
import com.och.calltask.domain.vo.CustomerCrowdVo;
import com.och.calltask.mapper.CustomerCrowdMapper;
import com.och.calltask.service.ICustomerCrowdService;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.common.utils.StringUtils;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 客户人群管理表(CustomerCrowd)表服务实现类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@RequiredArgsConstructor
@Service
public class CustomerCrowdServiceImpl extends BaseServiceImpl<CustomerCrowdMapper, CustomerCrowd> implements ICustomerCrowdService {

    private final ISysUserService sysUserService;

    @Override
    public void add(CustomerCrowdAddQuery query) {
        boolean checkName = checkName(query.getName());
        if (checkName) {
            throw new CommonException("名称已存在");
        }
        CustomerCrowd customerCrowd = new CustomerCrowd();
        customerCrowd.setName(query.getName());
        customerCrowd.setRemark(query.getRemark());
        customerCrowd.setSwipe(JSONObject.toJSONString(query.getSwipe()));
        customerCrowd.setStatus(query.getStatus());
        customerCrowd.setType(query.getType());
        customerCrowd.setProgress(1);
        save(customerCrowd);
    }

    @Override
    public void edit(CustomerCrowdAddQuery query) {
        CustomerCrowd crowd = getById(query.getId());
        if (Objects.isNull(crowd)) {
            throw new CommonException("无效ID");
        }
        if (!Objects.equals(crowd.getName(), query.getName()) && checkName(query.getName())) {
            throw new CommonException("名称已存在");
        } else if (StringUtils.isNotBlank(query.getName())) {
            crowd.setName(query.getName());
        }
        if (StringUtils.isNotBlank(query.getRemark())) {
            crowd.setRemark(query.getRemark());
        }
        if (CollectionUtils.isNotEmpty(query.getSwipe())) {
            crowd.setSwipe(JSONObject.toJSONString(query.getSwipe()));
        }
        if (Objects.nonNull(query.getStatus())) {
            crowd.setStatus(query.getStatus());
        }
        if (Objects.nonNull(query.getType())) {
            crowd.setType(query.getType());
        }
        updateById(crowd);

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(CustomerCrowdQuery query) {
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
        List<CustomerCrowd> list = ids.stream().map(id -> {
            CustomerCrowd customerCrowd = new CustomerCrowd();
            customerCrowd.setId(id);
            customerCrowd.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return customerCrowd;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public CustomerCrowdVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public List<CustomerCrowdVo> pageList(CustomerCrowdQuery query) {
        super.startPage(query.getPageIndex(), query.getPageSize());
        List<CustomerCrowdVo> list = getList(query);
        if (CollectionUtil.isEmpty(list)) {
            sysUserService.decorate(list);
        }
        return list;
    }

    @Override
    public List<CustomerCrowdVo> getList(CustomerCrowdQuery query) {
        return this.baseMapper.getList(query);
    }

    /**
     * 检查名称
     *
     * @param name 名称
     * @return 是否重复
     */
    private boolean checkName(String name) {
        long count = count(new LambdaQueryWrapper<CustomerCrowd>().eq(CustomerCrowd::getName, name).eq(BaseEntity::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        return count > 0;
    }
}

