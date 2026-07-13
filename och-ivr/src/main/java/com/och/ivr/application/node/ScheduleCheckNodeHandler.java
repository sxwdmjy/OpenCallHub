package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 时间检查节点处理器（策略模式）
 * <p>
 * 根据当前时间判断是否为工作时间（默认 09:00-18:00），
 * 选择对应的工作时间/非工作时间分支。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class ScheduleCheckNodeHandler implements IvrNodeHandler {

    /** 默认工作时间开始 */
    private static final LocalTime BIZ_START = LocalTime.of(9, 0);

    /** 默认工作时间结束 */
    private static final LocalTime BIZ_END = LocalTime.of(18, 0);

    @Override
    public IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                       List<IvrEdge> edges, Map<String, Object> variables) {
        if (edges == null || edges.isEmpty()) {
            return IvrNodeHandlerResult.end(variables);
        }

        boolean isBizHours = isBusinessHours();

        for (IvrEdge edge : edges) {
            if ("WORK_TIME".equals(edge.getConditionExpr()) && isBizHours) {
                log.debug("[SCHEDULE] 工作时间: target={}", edge.getTargetNodeId());
                return IvrNodeHandlerResult.continueTo(edge.getTargetNodeId(), variables);
            }
            if ("NON_WORK_TIME".equals(edge.getConditionExpr()) && !isBizHours) {
                log.debug("[SCHEDULE] 非工作时间: target={}", edge.getTargetNodeId());
                return IvrNodeHandlerResult.continueTo(edge.getTargetNodeId(), variables);
            }
        }

        log.warn("[SCHEDULE] 无匹配分支: isBizHours={}", isBizHours);
        return IvrNodeHandlerResult.end(variables);
    }

    /**
     * 判断当前是否为工作时间
     */
    private boolean isBusinessHours() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(BIZ_START) && !now.isAfter(BIZ_END);
    }
}