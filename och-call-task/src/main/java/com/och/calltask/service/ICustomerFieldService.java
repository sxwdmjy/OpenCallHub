package com.och.calltask.service;

import com.och.calltask.domain.entity.CustomerField;
import com.och.calltask.domain.query.CustomerFieldAddQuery;
import com.och.calltask.domain.query.CustomerFieldQuery;
import com.och.calltask.domain.vo.CustomerFieldVo;
import com.och.common.base.IBaseService;

import java.util.List;

/**
 * 客户字段管理表(CustomerField)表服务接口
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
public interface ICustomerFieldService extends IBaseService<CustomerField> {

    /**
     * 新增字段
     *
     * @param query 新增参数
     */
    void add(CustomerFieldAddQuery query);

    /**
     * 修改字段
     * @param query 修改参数
     */
    void edit(CustomerFieldAddQuery query);

    /**
     * 字段详情
     * @param id 字段ID
     * @return 字段详情
     */
    CustomerFieldVo getDetail(Long id);

    /**
     * 删除字段
     * @param query 查询参数
     */
    void delete(CustomerFieldQuery query);

    /**
     * 字段列表(分页)
     * @param query 查询参数
     * @return 字段列表
     */
    List<CustomerFieldVo> pageList(CustomerFieldQuery query);

    /**
     * 字段列表(不分页)
     * @param query 查询参数
     * @return 字段列表
     */
    List<CustomerFieldVo> getList(CustomerFieldQuery query);
}

