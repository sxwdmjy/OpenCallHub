package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.FsConfig;
import com.och.system.domain.query.fsconfig.FsConfigQuery;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * fs管理配置表(FsConfig)表数据库访问层
 *
 * @author danmo
 * @since 2023-10-17 11:04:58
 */
@Repository()
@Mapper
public interface FsConfigMapper extends BaseMapper<FsConfig> {

    List<FsConfig> getList(FsConfigQuery query);
}

