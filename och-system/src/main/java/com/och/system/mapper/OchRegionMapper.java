package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.OchRegion;

/**
 * (OchRegion)表数据库访问层
 *
 * @author danmo
 * @since 2025-05-26 17:11:09
 */
@Repository()
@Mapper
public interface OchRegionMapper extends BaseMapper<OchRegion> {

}

