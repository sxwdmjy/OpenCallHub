package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.OutboundDialAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 外呼拨号尝试数据访问层 */
@Mapper
public interface OutboundDialAttemptMapper extends BaseMapper<OutboundDialAttempt> {

    /** 根据去重键统计拨号尝试次数，用于幂等检查 */
    @Select("SELECT COUNT(*) FROM outbound_dial_attempt WHERE dedup_key = #{dedupKey}")
    int countByDedupKey(@Param("dedupKey") String dedupKey);
}
