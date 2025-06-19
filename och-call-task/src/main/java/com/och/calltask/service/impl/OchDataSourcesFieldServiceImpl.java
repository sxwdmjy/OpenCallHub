package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.entity.OchDataSourcesField;
import com.och.calltask.domain.vo.FieldInfoVo;
import com.och.calltask.service.IOchDataSourcesFieldService;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.calltask.mapper.OchDataSourcesFieldMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 数据源字段表(OchDataSourcesField)表服务实现类
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Service
public class OchDataSourcesFieldServiceImpl extends BaseServiceImpl<OchDataSourcesFieldMapper, OchDataSourcesField> implements IOchDataSourcesFieldService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBatchBySourceId(Long sourceId, List<Long> fieldIdList) {
        if(!CollectionUtil.isEmpty(fieldIdList)){
            List<OchDataSourcesField> sourcesFields = fieldIdList.stream().map(fieldId -> {
                OchDataSourcesField rel = new OchDataSourcesField();
                rel.setSourceId(sourceId);
                rel.setFieldId(fieldId);
                return rel;
            }).toList();
            saveBatch(sourcesFields);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBatchBySourceId(Long sourceId, List<Long> fieldIdList) {
        deleteBySourceId(Collections.singletonList(sourceId));
        saveBatchBySourceId(sourceId,fieldIdList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBySourceId(List<Long> sourceIds) {
        OchDataSourcesField ochDataSourcesField = new OchDataSourcesField();
        ochDataSourcesField.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        update(ochDataSourcesField,new LambdaQueryWrapper<OchDataSourcesField>().in(OchDataSourcesField::getSourceId,sourceIds));
    }

    @Override
    public List<FieldInfoVo> listBySourceId(Long sourceId) {
        if(Objects.isNull(sourceId)){
            return new ArrayList<>();
        }
        return this.baseMapper.listBySourceId(sourceId);
    }
}

