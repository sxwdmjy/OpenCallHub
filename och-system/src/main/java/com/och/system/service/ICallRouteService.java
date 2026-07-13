package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallRoute;
import com.och.system.domain.query.route.CallRouteAddQuery;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.vo.route.CallRouteVo;

import java.util.List;

/**
 * 号码路由管理服务接口
 * <p>提供号码路由规则的增删改查、启用/禁用管理功能。
 * 不同路由规则（按地区/按号码前缀/按技能组）可视为不同的路由策略，实现灵活的呼叫路由分发。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>策略模式</b>：不同路由规则（按地区路由/按号码路由/按技能组路由）作为可替换的路由策略，根据业务场景动态选择</li>
 *   <li><b>模板方法</b>：继承 IBaseService，复用通用 CRUD 模板</li>
 * </ul>
 *
 * @author danmo
 * @since 2024-12-30 14:03:41
 */
public interface ICallRouteService extends IBaseService<CallRoute> {

    void add(CallRouteAddQuery query);

    void edit(CallRouteAddQuery query);

    void delete(CallRouteQuery query);

    CallRouteVo getDetail(Long id);

    List<CallRouteVo> getPageList(CallRouteQuery query);
    List<CallRouteVo> getList(CallRouteQuery query);

    void enable(Long id);

    void disable(Long id);
}

