package com.och.acd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.acd.domain.AcdQueueSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACD队列会话数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，所有查询通过 LambdaQueryWrapper 在 Service 层构建。</p>
 *
 * @author danmo
 */
@Mapper
public interface AcdQueueSessionMapper extends BaseMapper<AcdQueueSession> {
}