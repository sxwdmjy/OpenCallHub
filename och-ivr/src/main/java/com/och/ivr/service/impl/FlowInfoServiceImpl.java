package com.och.ivr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.common.utils.StringUtils;
import com.och.ivr.domain.entity.FlowInfo;
import com.och.ivr.domain.query.FlowInfoAddQuery;
import com.och.ivr.domain.query.FlowInfoQuery;
import com.och.ivr.domain.vo.FlowInfoListVo;
import com.och.ivr.domain.vo.FlowInfoVo;
import com.och.ivr.mapper.FlowInfoMapper;
import com.och.ivr.service.IFlowInfoService;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * ivr流程信息(FlowInfo)表服务实现类
 *
 * @author danmo
 * @since 2024-12-23 15:08:24
 */
@RequiredArgsConstructor
@Service
public class FlowInfoServiceImpl extends BaseServiceImpl<FlowInfoMapper, FlowInfo> implements IFlowInfoService {

    private final ISysUserService sysUserService;
    private final ApplicationEventPublisher publisher;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FlowInfoAddQuery query) {
        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setName(query.getName());
        flowInfo.setDesc(query.getDesc());
        flowInfo.setStatus(query.getStatus());
        flowInfo.setGroupId(query.getGroupId());
        flowInfo.setFlowData(query.getFlowData());
        save(flowInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(FlowInfoAddQuery query) {
        FlowInfo flowInfo = getById(query.getId());
        if (Objects.isNull(flowInfo)) {
            throw new CommonException("无效ID");
        }
        if (!StringUtils.equals(flowInfo.getName(), query.getName()) && checkName(query.getName())) {
            throw new CommonException("名称已存在");
        } else {
            flowInfo.setName(query.getName());
        }
        if (StringUtils.isNotBlank(query.getDesc())) {
            flowInfo.setDesc(query.getDesc());
        }
        if (Objects.nonNull(query.getStatus())) {
            flowInfo.setStatus(query.getStatus());
        }
        if (StringUtils.isNotBlank(query.getFlowData())) {
            flowInfo.setFlowData(query.getFlowData());
        }
        updateById(flowInfo);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) {
        FlowInfo flowInfo = getById(id);
        if (Objects.isNull(flowInfo)) {
            throw new CommonException("无效ID");
        }
        flowInfo.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        updateById(flowInfo);
    }

    @Override
    public FlowInfoVo getInfo(Long id) {
        return baseMapper.getInfo(id);
    }

    @Override
    public List<FlowInfoListVo> pageList(FlowInfoQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        List<FlowInfoListVo> flowInfoList = this.baseMapper.getList(query);
        if (CollectionUtils.isNotEmpty(flowInfoList)) {
            sysUserService.decorate(flowInfoList);
        }
        return flowInfoList;
    }

    @Override
    public void publish(Long id) {
        FlowInfo flowInfo = getById(id);
        if (Objects.isNull(flowInfo)) {
            throw new CommonException("无效ID");
        }
        flowInfo.setStatus(2);
        updateById(flowInfo);

    }

    @Override
    public void offline(Long id) {
        FlowInfo flowInfo = getById(id);
        if (Objects.isNull(flowInfo)) {
            throw new CommonException("无效ID");
        }
        flowInfo.setStatus(1);
        updateById(flowInfo);
    }


    private boolean checkName(String name) {
        return null != getOne(new LambdaQueryWrapper<FlowInfo>().eq(FlowInfo::getName, name).last("limit 1"));
    }
}

