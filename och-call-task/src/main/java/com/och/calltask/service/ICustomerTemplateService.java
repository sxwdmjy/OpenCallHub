package com.och.calltask.service;

import com.github.pagehelper.PageInfo;
import com.och.calltask.domain.query.CustomerFieldQuery;
import com.och.calltask.domain.query.CustomerTemplateAddQuery;
import com.och.calltask.domain.query.CustomerTemplateQuery;
import com.och.calltask.domain.vo.CustomerTemplateListVo;
import com.och.calltask.domain.vo.CustomerTemplateVo;
import com.och.common.base.IBaseService;
import com.och.calltask.domain.entity.CustomerTemplate;

import java.util.List;

/**
 * 客户模板管理表(CustomerTemplate)表服务接口
 *
 * @author danmo
 * @since 2025-06-30 11:35:44
 */
public interface ICustomerTemplateService extends IBaseService<CustomerTemplate> {

    /**
     * 新增客户模板
     * @param query 新增参数
     */
    void add(CustomerTemplateAddQuery query);

    /**
     * 修改客户模板
     * @param query 修改参数
     */
    void edit(CustomerTemplateAddQuery query);

    /**
     * 删除客户模板
     * @param query 删除参数
     */
    void delete(CustomerTemplateQuery query);

    /**
     * 获取客户模板详情
     * @param id 客户模板ID
     * @return 客户模板详情
     */
    CustomerTemplateVo getDetail(Long id);

    /**
     * 获取客户模板列表(分页)
     * @param query 查询参数
     * @return 客户模板列表
     */
    PageInfo<CustomerTemplateVo> pageList(CustomerTemplateQuery query);

    /**
     * 获取客户模板列表(不分页)
     * @param query 查询参数
     * @return 客户模板列表
     */
    List<CustomerTemplateVo> getList(CustomerTemplateQuery query);
}

