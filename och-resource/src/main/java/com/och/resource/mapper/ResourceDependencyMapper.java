package com.och.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.resource.domain.ResourceDependency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资源依赖关系数据访问层，提供依赖关系的查询操作。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Mapper
public interface ResourceDependencyMapper extends BaseMapper<ResourceDependency> {

    @Select("SELECT * FROM res_dependency WHERE source_type = #{type} AND source_id = #{id}")
    List<ResourceDependency> findBySource(@Param("type") String type, @Param("id") Long id);

    @Select("SELECT * FROM res_dependency WHERE target_type = #{type} AND target_id = #{id}")
    List<ResourceDependency> findByTarget(@Param("type") String type, @Param("id") Long id);

    @Select("SELECT COUNT(*) FROM res_dependency WHERE target_type = #{type} AND target_id = #{id}")
    int countDependents(@Param("type") String type, @Param("id") Long id);
}
