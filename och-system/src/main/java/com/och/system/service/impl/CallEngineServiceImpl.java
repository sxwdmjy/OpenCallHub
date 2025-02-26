package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.CallEngine;
import com.och.system.domain.query.engine.CallEngineAddQuery;
import com.och.system.domain.query.engine.CallEngineQuery;
import com.och.system.mapper.CallEngineMapper;
import com.och.system.service.ICallEngineService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI引擎表(CallEngine)表服务实现类
 *
 * @author danmo
 * @since 2025-02-24 14:49:07
 */
@Service
public class CallEngineServiceImpl extends BaseServiceImpl<CallEngineMapper, CallEngine> implements ICallEngineService {

    @Override
    public void addEngine(CallEngineAddQuery query) {
        checkParams(query);
        CallEngine callEngine = new CallEngine();
        BeanUtils.copyProperties(query, callEngine);
        save(callEngine);
    }

    @Override
    public void edit(CallEngineAddQuery query) {
        checkParams(query);
        CallEngine callEngine = getById(query.getId());
        if(Objects.isNull(callEngine)){
            throw new RuntimeException("无效ID");
        }
        if(!Objects.equals(query.getName(), callEngine.getName())){
            callEngine.setName(query.getName());
        }
        if(!Objects.equals(query.getProfile(), callEngine.getProfile())){
            callEngine.setProfile(query.getProfile());
        }
        if(!Objects.equals(query.getType(), callEngine.getType())){
            callEngine.setType(query.getType());
        }
        if(!Objects.equals(query.getProviders(), callEngine.getProviders())){
            callEngine.setProviders(query.getProviders());
        }
        if(!Objects.equals(query.getTimbre(), callEngine.getTimbre())){
            callEngine.setTimbre(query.getTimbre());
        }
        if(!Objects.equals(query.getGrammar(), callEngine.getGrammar())){
            callEngine.setGrammar(query.getGrammar());
        }
        if(!Objects.equals(query.getParam(), callEngine.getParam())){
            callEngine.setParam(query.getParam());
        }
        updateById(callEngine);
    }



    @Override
    public void delete(CallEngineQuery query) {
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
        List<CallEngine> list = ids.stream().map(id -> {
            CallEngine callEngine = new CallEngine();
            callEngine.setId(id);
            callEngine.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return callEngine;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public CallEngine getDetail(Long id) {
        return getById(id);
    }

    @Override
    public List<CallEngine> getPageList(CallEngineQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }

    @Override
    public List<CallEngine> getList(CallEngineQuery query) {
        return this.baseMapper.getList(query);
    }

    private void checkParams(CallEngineAddQuery query) {
        List<CallEngine> callEngines = list(new LambdaQueryWrapper<CallEngine>().eq(CallEngine::getName, query.getName()).eq(BaseEntity::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        if(!CollectionUtils.isEmpty(callEngines)){
            throw new RuntimeException("名称已存在");
        }
    }


}

