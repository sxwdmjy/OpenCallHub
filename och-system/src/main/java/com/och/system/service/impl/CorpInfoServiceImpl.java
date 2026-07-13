package com.och.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.system.domain.entity.CorpInfo;
import com.och.system.domain.query.corp.CorpAddQuery;
import com.och.system.domain.query.corp.CorpQuery;
import com.och.system.mapper.CorpInfoMapper;
import com.och.system.service.ICorpInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 企业信息表(CorpInfo)
 *
 * @author danmo
 * @date 2023-07-07 14:37:57
 */
@Slf4j
@Service
public class CorpInfoServiceImpl extends BaseServiceImpl<CorpInfoMapper, CorpInfo> implements ICorpInfoService {


    @Override
    public void add(CorpAddQuery query) {
        CorpInfo corpInfo = new CorpInfo();
        BeanUtil.copyProperties(query, corpInfo);
        save(corpInfo);
    }

    @Override
    public void edit(CorpAddQuery query) {
        CorpInfo corpInfo = new CorpInfo();
        BeanUtil.copyProperties(query, corpInfo);
        corpInfo.setId(query.getCorpId());
        updateById(corpInfo);
    }

    @Override
    public void delete(CorpQuery query) {
        if(CollectionUtil.isNotEmpty(query.getCorpIds())){
            List<CorpInfo> corpInfos = query.getCorpIds().stream().map(id -> {
                CorpInfo corpInfo = new CorpInfo();
                corpInfo.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
                corpInfo.setId(id);
                return corpInfo;
            }).collect(Collectors.toList());
            updateBatchById(corpInfos);
        }
    }

    @Override
    public List<CorpInfo> getList(CorpQuery query) {
        List<CorpInfo> list = list(new LambdaQueryWrapper<CorpInfo>()
                .eq(CorpInfo::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex())
                .eq(Objects.nonNull(query.getStatus()), CorpInfo::getStatus, query.getStatus())
                .like(StringUtils.isNotEmpty(query.getCorpName()), CorpInfo::getCorpName, query.getCorpName())
                .eq(StringUtils.isNotEmpty(query.getCorpCode()), CorpInfo::getCorpCode, query.getCorpCode())
                .in(CollectionUtil.isNotEmpty(query.getCorpIds()),CorpInfo::getId,query.getCorpIds()));
        return list;
    }

    @Override
    public List<CorpInfo> getPageList(CorpQuery query) {
        startPage(query.getPageIndex(),query.getPageSize(),query.getSortField(),query.getSort());
        return getList(query);
    }


    @Override
    public CorpInfo getByCode(String corpCode) {
       return getOne(new LambdaQueryWrapper<CorpInfo>().eq(CorpInfo::getCorpCode,corpCode).eq(CorpInfo::getDelFlag,0));
    }
}
