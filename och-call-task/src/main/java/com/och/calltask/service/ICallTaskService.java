package com.och.calltask.service;

import com.och.calltask.domain.query.CallTaskAddQuery;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.common.base.IBaseService;
import com.och.calltask.domain.entity.CallTask;
import com.och.common.enums.CallTaskStatusEnum;

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

    /**
     * 暂停任务
     *
     * @param id 任务ID
     */
    void pauseTask(Long id);

    /**
     * 开始任务
     *
     * @param id 任务ID
     */
    void startTask(Long id);

    /**
     * 结束任务
     * @param id
     */
    void endTask(Long id);
    /**
     * 修改任务状态
     * @param id 任务ID
     * @param statusEnum 任务状态
     */
    Boolean updateStatus(Long id, CallTaskStatusEnum statusEnum);

}

