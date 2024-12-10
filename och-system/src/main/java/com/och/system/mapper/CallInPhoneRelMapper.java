package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.CallInPhoneRel;

/**
 * 呼入号码路由子码表(CallInPhoneRel)表数据库访问层
 *
 * @author danmo
 * @since 2024-12-10 10:29:11
 */
@Repository()
@Mapper
public interface CallInPhoneRelMapper extends BaseMapper<CallInPhoneRel> {

}

