package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallRoute;
import com.och.system.domain.query.route.CallRouteAddQuery;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.vo.route.CallRouteVo;

import java.util.List;

/**
 * 号码路由表(CallRoute)表服务接口
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

