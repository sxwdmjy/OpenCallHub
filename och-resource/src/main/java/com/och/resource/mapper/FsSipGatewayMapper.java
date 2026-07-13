package com.och.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SIP网关表(FsSipGateway)
 *
 * @author danmo
 * @date 2023-09-13 13:53:45
 */

@Mapper
public interface FsSipGatewayMapper extends BaseMapper<FsSipGateway> {


    List<FsSipGateway> getList(FsSipGatewayQuery query);
}

