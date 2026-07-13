package com.och.ivr.application.node;

import com.och.ivr.domain.IvrNodeType;

import java.util.HashMap;
import java.util.Map;

/**
 * IVR节点处理结果
 * <p>
 * 封装节点处理器执行后的结果，包含：下一节点ID、动作标记、节点类型和更新后的变量。
 * 调用方根据 action 字段决定后续流程（继续/排队/转接/挂断/结束）。
 * </p>
 *
 * @author danmo
 */
public class IvrNodeHandlerResult {

    public static final String VARIABLE_SKILL_GROUP_ID = "skillGroupId";
    public static final String VARIABLE_QUEUE_NODE_ID = "queueNodeId";

    /** 下一节点ID，null表示流程结束或需要外部处理 */
    private final String nextNodeId;

    /** 当需要外部处理时（如QUEUE/TRANSFER），标识节点类型 */
    private final IvrNodeType nextNodeType;

    /** 动作标记：CONTINUE/ENQUEUE/TRANSFER/HANGUP/END */
    private final String action;

    /** 本次执行后的变量映射 */
    private final Map<String, Object> variables;

    public IvrNodeHandlerResult(String nextNodeId, IvrNodeType nextNodeType,
                                String action, Map<String, Object> variables) {
        this.nextNodeId = nextNodeId;
        this.nextNodeType = nextNodeType;
        this.action = action;
        this.variables = variables;
    }

    /** 继续执行下一节点 */
    public static IvrNodeHandlerResult continueTo(String nextNodeId, Map<String, Object> variables) {
        return new IvrNodeHandlerResult(nextNodeId, null, "CONTINUE", variables);
    }

    /** 转入排队 */
    public static IvrNodeHandlerResult enqueue(Long skillGroupId, String queueNodeId, Map<String, Object> variables) {
        Map<String, Object> resultVariables = variables == null ? new HashMap<>() : variables;
        if (skillGroupId != null) {
            resultVariables.put(VARIABLE_SKILL_GROUP_ID, skillGroupId);
        }
        if (queueNodeId != null) {
            resultVariables.put(VARIABLE_QUEUE_NODE_ID, queueNodeId);
        }
        return new IvrNodeHandlerResult(null, IvrNodeType.QUEUE, "ENQUEUE", resultVariables);
    }

    /** 转入排队 */
    public static IvrNodeHandlerResult enqueue(Map<String, Object> variables) {
        return enqueue(null, null, variables);
    }

    /** 转接 */
    public static IvrNodeHandlerResult transfer(String target, Map<String, Object> variables) {
        Map<String, Object> resultVariables = variables == null ? new HashMap<>() : variables;
        if (target != null) {
            resultVariables.put("transferTarget", target);
        }
        return new IvrNodeHandlerResult(null, IvrNodeType.TRANSFER, "TRANSFER", resultVariables);
    }

    /** 转语音信箱 */
    public static IvrNodeHandlerResult voicemail(Map<String, Object> variables) {
        return new IvrNodeHandlerResult(null, IvrNodeType.TRANSFER_VOICEMAIL, "VOICEMAIL", variables);
    }

    /** 挂断 */
    public static IvrNodeHandlerResult hangup(Map<String, Object> variables) {
        return new IvrNodeHandlerResult(null, IvrNodeType.HANGUP, "HANGUP", variables);
    }

    /** 流程结束 */
    public static IvrNodeHandlerResult end(Map<String, Object> variables) {
        return new IvrNodeHandlerResult(null, null, "END", variables);
    }

    // Getters
    public String getNextNodeId() { return nextNodeId; }
    public IvrNodeType getNextNodeType() { return nextNodeType; }
    public String getAction() { return action; }
    public Map<String, Object> getVariables() { return variables; }

    public boolean isContinue() { return "CONTINUE".equals(action); }
    public boolean isTerminal() { return "END".equals(action) || "HANGUP".equals(action); }
}