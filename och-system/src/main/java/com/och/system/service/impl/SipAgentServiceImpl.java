package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.SipAgent;
import com.och.system.domain.query.agent.SipAgentAddQuery;
import com.och.system.domain.query.agent.SipAgentQuery;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.mapper.SipAgentMapper;
import com.och.system.service.ISipAgentService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 坐席管理表(SipAgent)
 *
 * @author danmo
 * @date 2023-09-26 11:08:58
 */
@Service
public class SipAgentServiceImpl extends BaseServiceImpl<SipAgentMapper, SipAgent> implements ISipAgentService {

    @Override
    public void add(SipAgentAddQuery query) {
        SipAgent lfsAgent = new SipAgent();
        lfsAgent.setUserId(query.getUserId());
        lfsAgent.setAgentNumber(query.getAgentNumber());
        save(lfsAgent);
    }

    @Override
    public void update(SipAgentAddQuery query) {
        SipAgent lfsAgent = new SipAgent();
        lfsAgent.setUserId(query.getUserId());
        lfsAgent.setAgentNumber(query.getAgentNumber());
        update(lfsAgent, new LambdaQueryWrapper<SipAgent>().eq(SipAgent::getId, query.getId()));
    }

    @Override
    public void delete(SipAgentQuery query) {
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
        List<SipAgent> list = ids.stream().map(id -> {
            SipAgent agent = new SipAgent();
            agent.setId(id);
            agent.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return agent;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public SipAgentVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public PageInfo<SipAgentVo> getPageList(SipAgentQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        List<SipAgentVo> resList = getInfoByQuery(query);
        return new PageInfo<>(resList);
    }

    @Override
    public List<SipAgentVo> getInfoByQuery(SipAgentQuery query) {
        return this.baseMapper.getInfoByQuery(query);
    }

    @Override
    public SipAgentVo getInfoByAgent(String agentNum) {
        return this.baseMapper.getInfoByAgent(agentNum);
    }


    @Override
    public Boolean updateStatus(Long id, Integer status) {
        SipAgent agent = new SipAgent();
        agent.setId(id);
        agent.setStatus(status);
        return updateById(agent);
    }

}
