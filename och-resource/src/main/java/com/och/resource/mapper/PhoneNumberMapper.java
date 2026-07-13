package com.och.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.resource.domain.PhoneNumber;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电话号码数据访问层，基于MyBatis-Plus提供基础CRUD操作。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Mapper
public interface PhoneNumberMapper extends BaseMapper<PhoneNumber> {
}
