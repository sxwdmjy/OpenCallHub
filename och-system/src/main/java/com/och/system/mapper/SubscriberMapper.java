package com.och.system.mapper;


import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.Subscriber;
import com.och.system.domain.query.subsriber.SubscriberQuery;
import com.och.system.domain.vo.sip.SubscriberVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Subscriber)
 *
 * @author danmo
 * @date 2023-08-29 10:49:24
 */
@Mapper
public interface SubscriberMapper extends BaseMapper<Subscriber> {


    Subscriber getByUserName(@Param("username") String username);


    List<SubscriberVo> getList(SubscriberQuery query);

    List<Subscriber> getByUserNameList(@Param("userNameList") List<String> userNameList);
}

