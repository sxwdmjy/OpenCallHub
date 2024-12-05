package com.och.system.service;


import com.och.common.base.IBaseService;
import com.och.system.domain.entity.KoDispatcher;
import com.och.system.domain.query.dispatcher.KoDispatcherAddQuery;
import com.och.system.domain.query.dispatcher.KoDispatcherQuery;

import java.util.List;

/**
 * (Dispatcher)
 *
 * @author danmo
 * @date 2024-07-29 10:49:18
 */
public interface IKoDispatcherService extends IBaseService<KoDispatcher> {

    void add(KoDispatcherAddQuery query);

    void edit(KoDispatcherAddQuery query);

    void delete(KoDispatcherQuery query);

    KoDispatcher getDetail(Integer id);

    List<KoDispatcher> getList(KoDispatcherQuery query);
    List<KoDispatcher> getPageList(KoDispatcherQuery query);


}
