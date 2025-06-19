package com.och.calltask.service;

import com.och.calltask.domain.query.CallTaskAddQuery;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.common.base.IBaseService;
import com.och.calltask.domain.entity.CallTask;

import java.util.List;

/**
 * 外呼任务表(CallTask)表服务接口
 *
 * @author danmo
 * @since 2025-06-18 15:53:57
 */
public interface ICallTaskService extends IBaseService<CallTask> {

    /**
     * 新增数据
     *
     * @param query 实例对象
     */
    void add(CallTaskAddQuery query);

    /**
     * 修改数据
     *
     * @param query 实例对象
     */
    void edit(CallTaskAddQuery query);

    /**
     * 删除数据
     *
     * @param query 删除条件
     */
    void detele(CallTaskQuery query);

    /**
     * 获取详情
     *
     * @param id 主键ID
     * @return 实体对象
     */
    CallTaskVo getDetail(Long id);

    /**
     * 获取列表(分页)
     *
     * @param query 查询条件
     * @return 列表
     */
    List<CallTaskVo> pageList(CallTaskQuery query);

    /**
     * 获取列表
     *
     * @param query 列表查询条件
     * @return 列表
     */
    List<CallTaskVo> getList(CallTaskQuery query);
}

