package com.och.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.customer.domain.CustomerProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户档案 Mapper，基于 MyBatis-Plus BaseMapper。
 * 提供按电话号码关联查询客户档案的能力。
 */
@Mapper
public interface CustomerProfileMapper extends BaseMapper<CustomerProfile> {

    @Select("SELECT cp.* FROM customer_profile cp " +
            "INNER JOIN customer_phone cp2 ON cp.id = cp2.customer_id " +
            "WHERE cp2.phone_number = #{phoneNumber} AND cp.deleted = 0 LIMIT 1")
    CustomerProfile findByPhone(@Param("phoneNumber") String phoneNumber);
}
