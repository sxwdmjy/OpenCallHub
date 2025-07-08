package com.och.calltask.service;

import com.och.calltask.domain.query.CustomerCrowdAddQuery;
import com.och.calltask.domain.query.CrowdCustomerQuery;
import com.och.calltask.domain.query.CustomerCrowdQuery;
import com.och.calltask.domain.vo.CrowdCustomerVo;
import com.och.calltask.domain.vo.CustomerCrowdVo;
import com.och.common.base.IBaseService;
import com.och.calltask.domain.entity.CustomerCrowd;

import java.util.List;

/**
 * 客户人群管理表(CustomerCrowd)表服务接口
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
public interface ICustomerCrowdService extends IBaseService<CustomerCrowd> {

    /**
     * 新增客户人群
     *
     * @param query 新增参数
     */
    void add(CustomerCrowdAddQuery query);

    /**
     * 修改客户人群
     *
     * @param query 修改参数
     */
    void edit(CustomerCrowdAddQuery query);

    /**
     * 删除客户人群
     *
     * @param query 删除参数
     */
    void delete(CustomerCrowdQuery query);

    /**
     * 获取客户人群详情
     *
     * @param id 客户人群ID
     * @return 客户人群详情
     */
    CustomerCrowdVo getDetail(Long id);

    /**
     * 获取客户人群列表(分页)
     *
     * @param query 查询参数
     * @return 客户人群列表
     */
    List<CustomerCrowdVo> pageList(CustomerCrowdQuery query);

    /**
     * 获取客户人群列表(不分页)
     *
     * @param query 获取参数
     * @return 客户人群列表
     */
    List<CustomerCrowdVo> getList(CustomerCrowdQuery query);

    /**
     * 获取客户人群客户列表(分页)
     *
     * @param query 获取参数
     * @return 客户人群客户列表
     */
    List<CrowdCustomerVo> pageCustomerList(CrowdCustomerQuery query);
}

