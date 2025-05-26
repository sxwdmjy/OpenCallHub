package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.PhoneLocation;

/**
 * (PhoneLocation)表数据库访问层
 *
 * @author danmo
 * @since 2025-05-26 17:12:25
 */
@Repository()
@Mapper
public interface PhoneLocationMapper extends BaseMapper<PhoneLocation> {

}

