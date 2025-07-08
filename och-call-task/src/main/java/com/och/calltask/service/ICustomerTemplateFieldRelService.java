package com.och.calltask.service;

import com.och.calltask.domain.query.CustomerTemplateFieldRelAddQuery;
import com.och.common.base.IBaseService;
import com.och.calltask.domain.entity.CustomerTemplateFieldRel;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 客户模板字段关联表(CustomerTemplateFieldRel)表服务接口
 *
 * @author danmo
 * @since 2025-06-30 11:35:45
 */
public interface ICustomerTemplateFieldRelService extends IBaseService<CustomerTemplateFieldRel> {

    /**
     * 通过模板ID批量保存
     *
     * @param templateId        模板ID
     * @param fieldList 字段列表
     */
    void saveByTemplateId(Long templateId, List<CustomerTemplateFieldRelAddQuery> fieldList);

    /**
     * 通过模板ID批量更新
     * @param templateId 模板ID
     * @param fieldList 字段列表
     */
    void updateByTemplateId(Long templateId, List<CustomerTemplateFieldRelAddQuery> fieldList);

    /**
     * 通过模板ID批量删除
     * @param templateId
     */
    void deleteByTemplateId(Long templateId);

    /**
     * 通过模板ID批量删除
     * @param templateIds
     */
    void deleteByTemplateId(List<Long> templateIds);
}

