package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.SysFile;

/**
 * 文件管理(SysFile)表数据库访问层
 *
 * @author danmo
 * @since 2024-11-18 10:22:33
 */
@Repository()
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {

}

