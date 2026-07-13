package com.och.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.resource.domain.AiPlugin;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI插件数据访问层，基于MyBatis-Plus提供基础CRUD操作。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Mapper
public interface AiPluginMapper extends BaseMapper<AiPlugin> {
}
