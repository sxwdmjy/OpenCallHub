package com.och.calltask.handler;

import com.alibaba.fastjson2.JSONArray;
import com.och.calltask.domain.entity.CallTask;
import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.query.CallTaskContactQuery;
import com.och.calltask.domain.vo.CallTaskContactVo;
import com.och.calltask.service.ICallTaskAssignmentService;
import com.och.calltask.service.ICallTaskService;
import com.och.common.utils.StringUtils;
import com.och.system.domain.vo.agent.SipAgentStatusVo;
import com.och.system.domain.vo.agent.SipSimpleAgent;
import com.och.system.service.ISipAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 预览外呼任务处理器
 *
 * @author danmo
 * @date 2025/06/26
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class PreviewOutboundHandler implements CallTaskHandler {

    private final ICallTaskService callTaskService;
    private final ISipAgentService sipAgentService;
    private final ICallTaskAssignmentService callTaskAssignmentService;

    @Override
    public void execute(Long taskId) {
        log.info("【任务分配】开始处理任务ID: {}", taskId);
        // 1. 获取任务基础信息
        CallTask callTask = callTaskService.getById(taskId);
        if (callTask == null) {
            log.warn("【任务异常】任务不存在, ID: {}", taskId);
            return;
        }


        // 3. 查询待分配客户
        List<CallTaskContactVo> taskContactList = getUnassignedContacts(taskId);
        if (CollectionUtils.isEmpty(taskContactList)) {
            log.info("【无待分配】任务:{} 当前无可分配客户", taskId);
            return;
        }

        // 4. 获取坐席配置
        List<Long> agentIds = parseAgentIds(callTask.getAgentList());
        List<SipAgentStatusVo> agentStatusList = sipAgentService.getAgentStatusList(agentIds);

        if (CollectionUtils.isEmpty(agentStatusList)) {
            log.warn("【无可用坐席】任务:{} 无有效坐席配置", taskId);
            return;
        }

        // 5. 执行分配逻辑
        List<CallTaskAssignment> assignments = new ArrayList<>();
        int roundRobinIndex = 0;

        for (CallTaskContactVo contact : taskContactList) {
            try {
                SipAgentStatusVo selectedAgent = null;

                // 循环查找可用坐席
                int maxAttempts = agentStatusList.size();
                for (int attempt = 0; attempt < maxAttempts; attempt++) {
                    selectedAgent = selectSuitableAgent(
                            agentStatusList,
                            callTask.getAssignmentType(),
                            roundRobinIndex++
                    );

                    if (selectedAgent == null) {
                        log.warn("【分配失败】客户:{} 无可用坐席", contact.getId());
                        break;
                    }

                    // 检查分配限制
                    if (checkAssignmentLimit(selectedAgent, callTask)) {
                        // 找到符合条件的坐席
                        break;
                    } else {
                        log.debug("【坐席已满】尝试下一个坐席，当前坐席ID:{}", selectedAgent.getId());
                        // 继续循环
                        selectedAgent = null;
                    }
                }

                if (selectedAgent == null) {
                    // 跳过当前客户
                    log.warn("【分配失败】客户:{} 无可用坐席", contact.getId());
                    continue;
                }

                // 创建分配记录
                assignments.add(createAssignment(contact, selectedAgent));

            } catch (Exception e) {
                log.error("【分配异常】客户:{} 分配失败", contact.getId(), e);
            }
        }
        // 6. 持久化分配结果
        if (!assignments.isEmpty()) {
            callTaskAssignmentService.updateBatchById(assignments);
            log.info("【分配完成】任务:{} 成功分配 {} 个客户", taskId, assignments.size());
        } else {
            log.warn("【无有效分配】任务:{} 未产生有效分配记录", taskId);
        }

    }

    /**
     * 获取未分配客户联系人
     */
    private List<CallTaskContactVo> getUnassignedContacts(Long taskId) {
        CallTaskContactQuery query = new CallTaskContactQuery();
        query.setStatus(0);
        query.setTaskId(taskId);
        return callTaskService.getTaskContactList(query);
    }

    /**
     * 解析坐席ID列表
     */
    private List<Long> parseAgentIds(String agentListJson) {
        if (!StringUtils.isNotBlank(agentListJson)) {
            return Collections.emptyList();
        }

        return JSONArray.parseArray(agentListJson, SipSimpleAgent.class)
                .stream()
                .map(SipSimpleAgent::getAgentId)
                .toList();
    }

    /**
     * 检查分配限制
     */
    private boolean checkAssignmentLimit(
            SipAgentStatusVo agent, CallTask callTask) {

        // 无优先限制直接通过
        if (Objects.equals(1, callTask.getIsPriority())) {
            return true;
        }

        // 有限制时检查上限
        CallTaskContactQuery query = new CallTaskContactQuery();
        query.setStatus(1);
        query.setTaskId(callTask.getId());
        query.setAgentIds(Collections.singletonList(agent.getId()));
        query.setCallStatus(0);
        List<CallTaskContactVo> taskContactList = callTaskService.getTaskContactList(query);
        if (CollectionUtils.isEmpty(taskContactList)) {
            return true;
        }
        if (taskContactList.size() >= callTask.getReceiveLimit()) {
            log.warn("【分配限制】任务:{} 已达到分配限制, 当前分配数:{}", callTask.getId(), taskContactList.size());
            return false;
        }
        return true;
    }

    /**
     * 选择合适坐席（支持自动更换）
     */
    private SipAgentStatusVo selectSuitableAgent(
            List<SipAgentStatusVo> agentStatusList,
            Integer assignmentType,
            int roundRobinIndex) {

        // 轮流分配
        if (assignmentType == 1) {
            return agentStatusList.get(roundRobinIndex % agentStatusList.size());
        }

        // 空闲优先分配（在线状态且最近空闲）
        if (assignmentType == 2) {
            return agentStatusList.stream()
                    .filter(agent -> agent.getOnlineStatus() == 1)
                    .max(Comparator.comparing(SipAgentStatusVo::getStatusTime))
                    .orElse(null);
        }
        log.warn("【未知分配类型】类型:{}", assignmentType);
        return null;
    }

    /**
     * 创建分配记录
     */
    private CallTaskAssignment createAssignment(CallTaskContactVo contact, SipAgentStatusVo agent) {
        CallTaskAssignment assignment = new CallTaskAssignment();
        assignment.setId(contact.getId());
        assignment.setAgentId(agent.getId());
        assignment.setStatus(1);
        assignment.setAssignmentTime(new Date());
        return assignment;
    }
}
