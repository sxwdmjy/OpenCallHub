package com.och.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.ai.domain.RobotFlow;
import com.och.ai.domain.RobotFlowVersion;
import com.och.ai.mapper.RobotFlowMapper;
import com.och.ai.mapper.RobotFlowVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机器人流程应用服务
 * <p>
 * Controller 只负责接口入口，流程创建、发布版本、删除等业务编排统一放在这里。
 * 发布时生成独立版本快照，避免线上机器人会话受草稿配置继续编辑影响。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotFlowApplicationServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.ai.mapper.RobotFlowMapper, com.och.ai.domain.RobotFlow> implements com.och.ai.service.IRobotFlowApplicationService {

    private static final String DEFAULT_ASR_PLUGIN = "DEFAULT_ASR";
    private static final String DEFAULT_TTS_PLUGIN = "DEFAULT_TTS";
    private static final String DEFAULT_BOT_TYPE = "INTERNAL";

    private final RobotFlowVersionMapper robotFlowVersionMapper;

    public List<RobotFlow> list() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<RobotFlow>()
                        .eq(RobotFlow::getEnabled, true)
                        .orderByDesc(RobotFlow::getCreatedAt)
        );
    }

    @Transactional
    public RobotFlow create(RobotFlow flow) {
        LocalDateTime now = LocalDateTime.now();
        if (flow.getBotType() == null || flow.getBotType().isBlank()) {
            flow.setBotType(DEFAULT_BOT_TYPE);
        }
        if (flow.getMaxTurns() == null) {
            flow.setMaxTurns(10);
        }
        if (flow.getSilenceTimeoutSec() == null) {
            flow.setSilenceTimeoutSec(5);
        }
        if (flow.getEnabled() == null) {
            flow.setEnabled(true);
        }
        flow.setCreatedAt(now);
        flow.setUpdatedAt(now);
        flow.setDeleted(0);
        baseMapper.insert(flow);
        return flow;
    }

    /**
     * 发布机器人流程版本。
     * <p>每次发布都会复制当前流程配置生成新版本，运行中的会话只引用已发布版本，
     * 后续编辑流程草稿不会影响历史会话和正在执行的机器人流程。</p>
     */
    @Transactional
    public RobotFlowVersion publish(Long flowId) {
        RobotFlow flow = baseMapper.selectById(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("机器人流程不存在: " + flowId);
        }

        RobotFlowVersion latest = robotFlowVersionMapper.selectOne(
                new LambdaQueryWrapper<RobotFlowVersion>()
                        .eq(RobotFlowVersion::getFlowId, flowId)
                        .orderByDesc(RobotFlowVersion::getVersionNo)
                        .last("LIMIT 1")
        );
        int nextVersionNo = latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;

        RobotFlowVersion version = new RobotFlowVersion();
        version.setFlowId(flowId);
        version.setVersionNo(nextVersionNo);
        version.setLlmPluginCode(resolveBotPluginCode(flow.getBotType()));
        version.setAsrPluginCode(DEFAULT_ASR_PLUGIN);
        version.setTtsPluginCode(DEFAULT_TTS_PLUGIN);
        version.setSystemPrompt(buildSystemPrompt(flow));
        version.setIntentDefinitionsJson("[]");
        version.setSlotDefinitionsJson("{}");
        version.setTransferRulesJson(buildTransferRulesJson(flow.getTransferCondition()));
        version.setState("PUBLISHED");
        version.setCreatedAt(LocalDateTime.now());
        version.setPublishedAt(LocalDateTime.now());
        robotFlowVersionMapper.insert(version);

        log.info("[AI流程] 发布机器人流程版本: flowId={}, versionNo={}, botType={}",
                flowId, nextVersionNo, flow.getBotType());
        return version;
    }

    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }

    private String resolveBotPluginCode(String botType) {
        return botType == null || botType.isBlank() ? DEFAULT_BOT_TYPE : botType;
    }

    private String buildSystemPrompt(RobotFlow flow) {
        if (flow.getGreetingText() != null && !flow.getGreetingText().isBlank()) {
            return "你是 openCallHub 电话客服机器人。开场白：" + flow.getGreetingText();
        }
        return "你是 openCallHub 电话客服机器人，请用简洁、礼貌、专业的语气与客户沟通。";
    }

    private String buildTransferRulesJson(String transferCondition) {
        if (transferCondition == null || transferCondition.isBlank()) {
            return "{}";
        }
        String escaped = transferCondition.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"conditions\":\"" + escaped + "\"}";
    }
}
