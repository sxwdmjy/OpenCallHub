package com.och.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.customer.domain.CustomerPhone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 客户电话 Mapper，基于 MyBatis-Plus BaseMapper。
 * 提供按客户ID查询电话列表的能力。
 */
@Mapper
public interface CustomerPhoneMapper extends BaseMapper<CustomerPhone> {

    @Select("SELECT * FROM customer_phone WHERE customer_id = #{customerId}")
    List<CustomerPhone> findByCustomerId(@Param("customerId") Long customerId);
}
