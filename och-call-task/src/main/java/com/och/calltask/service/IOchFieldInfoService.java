package com.och.calltask.service;

import com.och.calltask.domain.entity.OchFieldInfo;
import com.och.calltask.domain.query.FieldAddQuery;
import com.och.calltask.domain.query.FieldQuery;
import com.och.calltask.domain.vo.FieldInfoVo;
import com.och.common.base.IBaseService;

import java.util.List;

/**
 * 字段管理表(OchFieldInfo)表服务接口
 *
 * @author danmo
 * @since 2025-06-16 14:53:21
 */
public interface IOchFieldInfoService extends IBaseService<OchFieldInfo> {

    /**
     * 新增字段
     *
     * @param query 新增字段参数
     */
    void add(FieldAddQuery query);

    /**
     * 修改字段
     *
     * @param query 修改字段参数
     */
    void edit(FieldAddQuery query);

    /**
     * 字段详情
     *
     * @param id 字段ID
     * @return 字段详情
     */
    FieldInfoVo get(Long id);

    /**
     * 删除字段
     *
     * @param query 删除字段参数
     */
    void delete(FieldQuery query);

    /**
     * 字段列表(分页)
     *
     * @param query 查询参数
     * @return 字段列表
     */
    List<FieldInfoVo> pageList(FieldQuery query);

    /**
     * 字段列表(不分页)
     *
     * @param query 列表查询参数
     * @return 字段列表
     */
    List<FieldInfoVo> getList(FieldQuery query);
}

