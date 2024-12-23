package com.och.ivr.service;

import com.och.common.base.IBaseService;
import com.och.ivr.domain.entity.FlowInfo;
import com.och.ivr.domain.query.FlowInfoAddQuery;
import com.och.ivr.domain.vo.FlowInfoVo;

/**
 * ivr流程信息(FlowInfo)表服务接口
 *
 * @author danmo
 * @since 2024-12-23 15:08:24
 */
public interface IFlowInfoService extends IBaseService<FlowInfo> {

    void add(FlowInfoAddQuery query);

    void edit(FlowInfoAddQuery query);

    void delete(Long id);

    FlowInfoVo getInfo(Long id);
}

