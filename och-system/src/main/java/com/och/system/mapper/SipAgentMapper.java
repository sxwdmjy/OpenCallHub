package com.och.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.SipAgent;
import com.och.system.domain.query.agent.SipAgentQuery;
import com.och.system.domain.vo.agent.SipAgentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 坐席管理表(SipAgent)
 *
 * @author danmo
 * @date 2023-09-26 11:08:58
 */

@Mapper
public interface SipAgentMapper extends BaseMapper<SipAgent> {


    List<SipAgentVo> getInfoByQuery(SipAgentQuery query);

    SipAgentVo getInfoByAgent(@Param("agentNum") String agentNum);

    SipAgentVo getDetail(@Param("id") Long id);

}

