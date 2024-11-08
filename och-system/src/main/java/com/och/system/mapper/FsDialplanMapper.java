package com.och.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.FsDialplan;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.vo.dialplan.FsDialplanVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * fs拨号计划表(FsDialplan)
 *
 * @author danmo
 * @date 2023-09-15 11:04:19
 */

@Mapper
public interface FsDialplanMapper extends BaseMapper<FsDialplan> {


    List<FsDialplanVo> getList(FsDialplanQuery query);
}

