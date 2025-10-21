package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.ai.domain.IntentInfo;

/**
 * 意图信息(IntentInfo)表数据库访问层
 *
 * @author danmo
 * @since 2025-10-17 14:54:11
 */
@Repository()
@Mapper
public interface IntentInfoMapper extends BaseMapper<IntentInfo> {

}

