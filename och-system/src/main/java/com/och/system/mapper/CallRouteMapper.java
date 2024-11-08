package com.och.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.CallRoute;
import com.och.system.domain.query.route.CallRouteQuery;
import com.och.system.domain.vo.route.CallRouteListVo;
import com.och.system.domain.vo.route.CallRouteVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 号码路由表(CallRoute)表数据库访问层
 *
 * @author danmo
 * @since 2023-10-18 14:21:22
 */
@Repository()
@Mapper
public interface CallRouteMapper extends BaseMapper<CallRoute> {

    CallRouteVo getDetail(@Param("id") Long id);

    List<CallRouteListVo> getList(CallRouteQuery query);

    List<CallRouteVo> listByQuery(CallRouteQuery query);
}

