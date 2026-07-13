package com.och.compliance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.compliance.domain.DncRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * DNC 记录 Mapper，基于 MyBatis-Plus BaseMapper。
 * 提供检查号码是否在 DNC 列表中的能力（含过期时间判断）。
 */
@Mapper
public interface DncRecordMapper extends BaseMapper<DncRecord> {

    @Select("SELECT COUNT(*) FROM compliance_dnc WHERE phone_number = #{phone} AND enabled = 1 " +
            "AND (expire_at IS NULL OR expire_at > NOW())")
    int isDncNumber(@Param("phone") String phone);
}
