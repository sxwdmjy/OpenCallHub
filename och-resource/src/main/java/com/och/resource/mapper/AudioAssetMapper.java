package com.och.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.resource.domain.AudioAsset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 音频素材数据访问层，基于MyBatis-Plus提供基础CRUD操作。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Mapper
public interface AudioAssetMapper extends BaseMapper<AudioAsset> {
}
