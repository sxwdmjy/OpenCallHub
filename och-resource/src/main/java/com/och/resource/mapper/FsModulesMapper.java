package com.och.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.FsModules;
import com.och.system.domain.query.modules.FsModulesQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * fs模块管理表(FsModules)
 *
 * @author danmo
 * @date 2023-09-15 15:42:28
 */

@Mapper
public interface FsModulesMapper extends BaseMapper<FsModules> {

    List<FsModules> getList(FsModulesQuery query);
}

