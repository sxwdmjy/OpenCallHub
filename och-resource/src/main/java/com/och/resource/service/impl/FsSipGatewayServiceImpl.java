package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fssip.FsSipGatewayAddQuery;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import com.och.system.mapper.FsSipGatewayMapper;
import com.och.system.service.IFsSipGatewayService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SIP网关表(FsSipGateway)
 *
 * @author danmo
 * @date 2023-09-13 13:53:45
 */
@Service
public class FsSipGatewayServiceImpl extends BaseServiceImpl<FsSipGatewayMapper, FsSipGateway> implements IFsSipGatewayService {


    @Override
    public void add(FsSipGatewayAddQuery query) {
        FsSipGateway sipGateway = new FsSipGateway();
        BeanUtil.copyProperties(query, sipGateway);
        save(sipGateway);
    }

    @Override
    public void edit(FsSipGatewayAddQuery query) {
        FsSipGateway sipGateway = getById(query.getId());
        if (Objects.isNull(sipGateway)) {
            throw new BizException(ErrorCode.BIZ_RESOURCE_ID_REQUIRED);
        }
        FsSipGateway updateSipGateway = new FsSipGateway();
        BeanUtil.copyProperties(query, updateSipGateway);
        updateSipGateway.setId(query.getId());
        updateById(updateSipGateway);
    }

    @Override
    public FsSipGateway getDetail(Long id) {
        return getById(id);
    }

    @Override
    public void delete(FsSipGatewayQuery query) {
        List<Long> ids = new LinkedList<>();
        if (Objects.nonNull(query.getId())) {
            ids.add(query.getId());
        }
        if (CollectionUtil.isNotEmpty(query.getIds())) {
            ids.addAll(query.getIds());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        List<FsSipGateway> list = ids.stream().map(id -> {
            FsSipGateway fsSipGateway = new FsSipGateway();
            fsSipGateway.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            fsSipGateway.setId(id);
            return fsSipGateway;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public List<FsSipGateway> getList(FsSipGatewayQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public List<FsSipGateway> getPageList(FsSipGatewayQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }
}
