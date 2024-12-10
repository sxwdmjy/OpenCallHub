package com.och.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.query.callin.CallInPhoneQuery;
import com.och.system.domain.vo.callin.CallInPhoneVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.CallInPhone;

import java.util.List;

/**
 * 呼入号码表(CallInPhone)表数据库访问层
 *
 * @author danmo
 * @since 2024-12-10 10:29:11
 */
@Repository()
@Mapper
public interface CallInPhoneMapper extends BaseMapper<CallInPhone> {

    CallInPhoneVo getDetail(CallInPhoneQuery query);

    List<CallInPhoneVo> getList(CallInPhoneQuery query);

    CallInPhoneVo getDetailByPhone(@Param("phone") String phone);
}

