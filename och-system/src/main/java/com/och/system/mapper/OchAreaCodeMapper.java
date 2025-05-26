package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.OchAreaCode;

/**
 * 基于location_gaode手工整理后的表(用于匹配区号)(OchAreaCode)表数据库访问层
 *
 * @author danmo
 * @since 2025-05-26 17:10:01
 */
@Repository()
@Mapper
public interface OchAreaCodeMapper extends BaseMapper<OchAreaCode> {

}

