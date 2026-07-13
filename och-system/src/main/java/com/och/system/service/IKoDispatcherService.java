package com.och.system.service;


import com.och.common.base.IBaseService;
import com.och.system.domain.entity.KoDispatcher;
import com.och.system.domain.query.dispatcher.KoDispatcherAddQuery;
import com.och.system.domain.query.dispatcher.KoDispatcherQuery;

import java.util.List;

/**
 * Kamailio 调度器管理服务接口
 * <p>提供 Kamailio 负载均衡调度器的增删改查功能。
 * 支持多种调度算法（哈希/轮询/优先级），实现灵活的 SIP 流量分发。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>策略模式</b>：Kamailio 负载均衡支持多种调度算法（哈希/轮询/优先级），各算法作为独立策略可灵活切换</li>
 *   <li><b>模板方法</b>：继承 IBaseService，复用通用 CRUD 模板</li>
 * </ul>
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
