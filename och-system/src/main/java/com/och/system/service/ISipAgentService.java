package com.och.system.service;

import com.github.pagehelper.PageInfo;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.SipAgent;
import com.och.system.domain.query.agent.SipAgentAddQuery;
import com.och.system.domain.query.agent.SipAgentQuery;
import com.och.system.domain.vo.agent.SipAgentStatusVo;
import com.och.system.domain.vo.agent.SipAgentVo;

import java.util.List;

/**
 * 坐席管理表(SipAgent)
 *
 * @author danmo
 * @date 2023-09-26 11:08:58
 */
public interface ISipAgentService extends IBaseService<SipAgent> {

    void add(SipAgentAddQuery query);

    void update(SipAgentAddQuery query);

    void delete(SipAgentQuery query);

    SipAgentVo getDetail(Long id);

    PageInfo<SipAgentVo> getPageList(SipAgentQuery query);

    List<SipAgentVo> getInfoByQuery(SipAgentQuery query);

    SipAgentVo getInfoByAgent(String agentNum);

    Boolean updateStatus(Long id, Integer status);

    List<SipAgentStatusVo> getAgentStatusList(List<Long> agentIds);

    void updateOnlineStatus(Long id, Integer onlineStatus, Long timestamp);
}
