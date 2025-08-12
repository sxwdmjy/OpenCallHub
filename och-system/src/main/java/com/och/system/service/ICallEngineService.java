package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallEngine;
import com.och.system.domain.query.engine.CallEngineAddQuery;
import com.och.system.domain.query.engine.CallEngineQuery;

import java.util.List;

/**
 * AI引擎表(CallEngine)表服务接口
 *
 * @author danmo
 * @since 2025-02-24 14:49:07
 */
public interface ICallEngineService extends IBaseService<CallEngine> {

    void addEngine(CallEngineAddQuery query);

    void edit(CallEngineAddQuery query);

    void delete(CallEngineQuery query);


    CallEngine getDetail(Long id);

    List<CallEngine> getPageList(CallEngineQuery query);

    List<CallEngine> getList(CallEngineQuery query);
}

