package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.OchDataSourcesField;
import com.och.system.domain.vo.calltask.FieldInfoVo;

import java.util.List;

/**
 * 数据源字段表(OchDataSourcesField)表服务接口
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
public interface IOchDataSourcesFieldService extends IBaseService<OchDataSourcesField> {

    /**
     * 通过数据源ID查询字段列表
     *
     * @param sourceId 数据源ID
     * @return 字段列表
     */
    void saveBatchBySourceId(Long sourceId, List<Long> fieldIdList);

    /**
     * 通过数据源ID查询字段列表
     *
     * @param sourceId 数据源ID
     * @return 字段列表
     */
    void updateBatchBySourceId(Long sourceId, List<Long> fieldIdList);

    /**
     * 通过数据源ID删除字段列表
     *
     * @param sourceIds 数据源ID
     */
    void deleteBySourceId(List<Long> sourceIds);

    /**
     * 通过数据源ID查询字段列表
     *
     * @param sourceId 数据源ID
     * @return 字段列表
     */
    List<FieldInfoVo> listBySourceId(Long sourceId);
}

