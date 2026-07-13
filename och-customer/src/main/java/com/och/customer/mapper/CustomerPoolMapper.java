package com.och.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.customer.domain.CustomerPool;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户池 Mapper，基于 MyBatis-Plus BaseMapper。
 */
@Mapper
public interface CustomerPoolMapper extends BaseMapper<CustomerPool> {
}
