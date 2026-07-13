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
 * 坐席管理服务接口
 * <p>提供 SIP 坐席的增删改查、在线状态同步与状态查询功能。
 * 坐席状态变更通过 Redis 实时通知 ACD 模块，实现呼叫中心的坐席管理与分配。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>观察者模式</b>：坐席状态变更通过 Redis 发布/订阅机制实时通知 ACD 模块，实现状态变化的解耦通知</li>
 *   <li><b>模板方法</b>：继承 IBaseService，复用通用 CRUD 模板</li>
 * </ul>
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
