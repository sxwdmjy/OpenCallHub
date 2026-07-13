package com.och.compliance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.compliance.domain.BlacklistRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 黑名单记录 Mapper，基于 MyBatis-Plus BaseMapper。
 * 提供检查号码是否在黑名单中的能力。
 */
@Mapper
public interface BlacklistRecordMapper extends BaseMapper<BlacklistRecord> {

    @Select("SELECT COUNT(*) FROM compliance_blacklist WHERE phone_number = #{phone} AND enabled = 1")
    int isBlacklisted(@Param("phone") String phone);
}
